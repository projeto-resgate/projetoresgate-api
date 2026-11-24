package com.projetoresgate.projetoresgate_api.infrastructure.email;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JavaMailEmailService {

    private static final Logger log = LoggerFactory.getLogger(JavaMailEmailService.class);

    interface DelayedExecutor {
        void execute(Runnable task, Duration delay);
    }

    private record EmailMessage(String to, String subject, String htmlContent) {
    }

    private final JavaMailSender mailSender;
    private final String sender;
    private final int maxAttempts;
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final DelayedExecutor executor;
    private final ScheduledThreadPoolExecutor ownedExecutor;
    private final Counter successCounter;
    private final Counter retryCounter;
    private final Counter failureCounter;

    @Autowired
    public JavaMailEmailService(
            JavaMailSender mailSender,
            MeterRegistry meterRegistry,
            @Value("${spring.mail.username}") String sender,
            @Value("${app.mail.max-attempts:3}") int maxAttempts,
            @Value("${app.mail.retry-delay-seconds:10}") long retryDelaySeconds,
            @Value("${app.mail.retry-backoff-multiplier:4.0}") double backoffMultiplier) {
        this(mailSender, meterRegistry, sender, maxAttempts, Duration.ofSeconds(retryDelaySeconds),
                backoffMultiplier, null);
    }

    JavaMailEmailService(JavaMailSender mailSender, MeterRegistry meterRegistry, String sender, int maxAttempts,
                         Duration initialDelay, double backoffMultiplier, DelayedExecutor executor) {
        this.mailSender = mailSender;
        this.sender = sender;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialDelay = initialDelay;
        this.backoffMultiplier = Math.max(1.0, backoffMultiplier);
        if (executor == null) {
            this.ownedExecutor = newExecutor();
            this.executor = (task, delay) -> ownedExecutor.schedule(task, delay.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            this.ownedExecutor = null;
            this.executor = executor;
        }
        this.successCounter = counter(meterRegistry, "success");
        this.retryCounter = counter(meterRegistry, "retry");
        this.failureCounter = counter(meterRegistry, "failure");
    }

    public void sendHtml(String to, String subject, String htmlContent) {
        EmailMessage message = new EmailMessage(to, subject, htmlContent);
        Runnable dispatch = () -> executor.execute(() -> attempt(message, 1), Duration.ZERO);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch.run();
                }
            });
        } else {
            dispatch.run();
        }
    }

    private void attempt(EmailMessage message, int attemptNumber) {
        try {
            deliver(message);
            successCounter.increment();
            log.info("E-mail enviado: destinatario={} assunto=\"{}\" tentativa={}",
                    mask(message.to()), message.subject(), attemptNumber);
        } catch (Exception e) {
            handleFailure(message, attemptNumber, e);
        }
    }

    private void handleFailure(EmailMessage message, int attemptNumber, Exception error) {
        boolean permanent = isPermanent(error);
        if (permanent || attemptNumber >= maxAttempts) {
            failureCounter.increment();
            log.error("Falha ao enviar e-mail, desistindo: destinatario={} assunto=\"{}\" tentativa={}/{} motivo={}",
                    mask(message.to()), message.subject(), attemptNumber, maxAttempts,
                    permanent ? "erro permanente" : "tentativas esgotadas", error);
            return;
        }

        Duration delay = delayAfter(attemptNumber);
        retryCounter.increment();
        log.warn("Falha ao enviar e-mail, nova tentativa em {}s: destinatario={} assunto=\"{}\" tentativa={}/{} erro={}",
                delay.toSeconds(), mask(message.to()), message.subject(), attemptNumber, maxAttempts,
                rootMessage(error));
        executor.execute(() -> attempt(message, attemptNumber + 1), delay);
    }

    private void deliver(EmailMessage message) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(sender);
        helper.setTo(message.to());
        helper.setSubject(message.subject());
        helper.setText(message.htmlContent(), true);
        mailSender.send(mimeMessage);
    }

    Duration delayAfter(int failedAttempt) {
        double factor = Math.pow(backoffMultiplier, failedAttempt - 1);
        return Duration.ofMillis((long) (initialDelay.toMillis() * factor));
    }

    static boolean isPermanent(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof MailAuthenticationException || t instanceof MailParseException
                    || t instanceof AddressException) {
                return true;
            }
            if (t instanceof SendFailedException sfe && sfe.getInvalidAddresses() != null
                    && sfe.getInvalidAddresses().length > 0) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }

    static String mask(String email) {
        if (email == null || email.isBlank()) {
            return "(vazio)";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    private static Counter counter(MeterRegistry registry, String result) {
        return Counter.builder("mail.delivery")
                .description("Resultado do envio de e-mails (success, retry, failure)")
                .tag("result", result)
                .register(registry);
    }

    private static ScheduledThreadPoolExecutor newExecutor() {
        AtomicInteger sequence = new AtomicInteger();
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(2, runnable -> {
            Thread thread = new Thread(runnable, "email-sender-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        pool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        pool.setRemoveOnCancelPolicy(true);
        return pool;
    }

    @PreDestroy
    void shutdown() throws InterruptedException {
        if (ownedExecutor == null) {
            return;
        }
        ownedExecutor.shutdown();
        if (!ownedExecutor.awaitTermination(15, TimeUnit.SECONDS)) {
            List<Runnable> dropped = ownedExecutor.shutdownNow();
            if (!dropped.isEmpty()) {
                log.warn("{} e-mail(s) pendente(s) foram descartados no desligamento", dropped.size());
            }
        }
    }
}
