package com.projetoresgate.projetoresgate_api.infrastructure.email;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.mail.Address;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("JavaMailEmailService - Test")
class JavaMailEmailServiceTest {

    private static final class FakeExecutor implements JavaMailEmailService.DelayedExecutor {
        final Queue<Runnable> queue = new ArrayDeque<>();
        final List<Duration> delays = new ArrayList<>();

        @Override
        public void execute(Runnable task, Duration delay) {
            delays.add(delay);
            queue.add(task);
        }

        void runAll() {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    private JavaMailSender mailSender;
    private MeterRegistry registry;
    private FakeExecutor executor;
    private JavaMailEmailService service;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));
        registry = new SimpleMeterRegistry();
        executor = new FakeExecutor();
        service = new JavaMailEmailService(mailSender, registry, "no-reply@projetoresgate.org", 3,
                Duration.ofSeconds(10), 4.0, executor);
    }

    @AfterEach
    void cleanTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private double count(String result) {
        return registry.counter("mail.delivery", "result", result).count();
    }

    @Test
    @DisplayName("Deve enviar ao destinatário informado, com remetente, assunto e corpo HTML")
    void shouldSendToRequestedRecipient() throws Exception {
        service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");
        executor.runAll();

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        MimeMessage sent = captor.getValue();
        assertEquals("maria@test.com", ((InternetAddress) sent.getAllRecipients()[0]).getAddress());
        assertEquals("no-reply@projetoresgate.org", ((InternetAddress) sent.getFrom()[0]).getAddress());
        assertEquals("Assunto", sent.getSubject());
        assertEquals(1.0, count("success"));
    }

    @Test
    @DisplayName("sendHtml deve retornar sem enviar na hora (o envio é assíncrono)")
    void shouldNotSendSynchronously() {
        service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");

        verifyNoInteractions(mailSender);
        assertEquals(List.of(Duration.ZERO), executor.delays);
    }

    @Test
    @DisplayName("Falha transitória: deve tentar de novo com atraso e concluir quando o SMTP voltar")
    void shouldRetryTransientFailureThenSucceed() {
        doThrow(new MailSendException("SMTP fora do ar")).doNothing().when(mailSender).send(any(MimeMessage.class));

        service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");
        executor.runAll();

        verify(mailSender, times(2)).send(any(MimeMessage.class));
        assertEquals(List.of(Duration.ZERO, Duration.ofSeconds(10)), executor.delays);
        assertEquals(1.0, count("retry"));
        assertEquals(1.0, count("success"));
        assertEquals(0.0, count("failure"));
    }

    @Test
    @DisplayName("Deve usar backoff exponencial e desistir ao esgotar as tentativas, sem lançar exceção")
    void shouldGiveUpAfterMaxAttemptsWithBackoff() {
        doThrow(new MailSendException("SMTP fora do ar")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> {
            service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");
            executor.runAll();
        });

        verify(mailSender, times(3)).send(any(MimeMessage.class));
        assertEquals(List.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(40)), executor.delays);
        assertEquals(2.0, count("retry"));
        assertEquals(1.0, count("failure"));
        assertEquals(0.0, count("success"));
    }

    @Test
    @DisplayName("Falha permanente (credencial inválida) não deve ser repetida")
    void shouldNotRetryPermanentFailure() {
        doThrow(new MailAuthenticationException("senha errada")).when(mailSender).send(any(MimeMessage.class));

        service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");
        executor.runAll();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertEquals(0.0, count("retry"));
        assertEquals(1.0, count("failure"));
    }

    @Test
    @DisplayName("Dentro de uma transação, só deve enviar depois do commit")
    void shouldWaitForCommit() {
        TransactionSynchronizationManager.initSynchronization();

        service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");

        assertTrue(executor.queue.isEmpty(), "nada agendado antes do commit");
        List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, syncs.size());

        syncs.forEach(TransactionSynchronization::afterCommit);
        executor.runAll();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Se a transação sofrer rollback, nenhum e-mail deve sair")
    void shouldNotSendOnRollback() {
        TransactionSynchronizationManager.initSynchronization();

        service.sendHtml("maria@test.com", "Assunto", "<p>Olá</p>");
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        executor.runAll();

        verifyNoInteractions(mailSender);
    }

    @Test
    @DisplayName("O retry deve esperar o tempo do backoff configurado")
    void delayAfter_shouldGrowExponentially() {
        assertEquals(Duration.ofSeconds(10), service.delayAfter(1));
        assertEquals(Duration.ofSeconds(40), service.delayAfter(2));
        assertEquals(Duration.ofSeconds(160), service.delayAfter(3));
    }

    @Test
    @DisplayName("Deve mascarar o e-mail nos logs")
    void mask_shouldHideAddress() {
        assertEquals("m***@gmail.com", JavaMailEmailService.mask("maria@gmail.com"));
        assertEquals("***", JavaMailEmailService.mask("sem-arroba"));
        assertEquals("(vazio)", JavaMailEmailService.mask(null));
        assertEquals("(vazio)", JavaMailEmailService.mask(" "));
    }

    @Test
    @DisplayName("Deve classificar erros permanentes e transitórios")
    void isPermanent_shouldClassify() {
        assertTrue(JavaMailEmailService.isPermanent(new MailAuthenticationException("x")));
        assertTrue(JavaMailEmailService.isPermanent(new MailSendException("x", new AddressException("inválido"))));
        SendFailedException invalid = new SendFailedException("x", null, new Address[0], new Address[0],
                new Address[]{new InternetAddress()});
        assertTrue(JavaMailEmailService.isPermanent(new MailSendException("x", invalid)));

        assertFalse(JavaMailEmailService.isPermanent(new MailSendException("timeout")));
        assertFalse(JavaMailEmailService.isPermanent(new RuntimeException("conexão recusada")));
    }
}
