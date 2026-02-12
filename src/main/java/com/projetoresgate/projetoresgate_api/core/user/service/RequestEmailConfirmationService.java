package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RequestEmailConfirmationService implements RequestEmailConfirmationUseCase {

    private final UserRepository userRepository;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;
    private final JavaMailEmailService javaMailEmailService;

    public RequestEmailConfirmationService(UserRepository userRepository,
                                           EmailConfirmationTokenRepository emailConfirmationTokenRepository,
                                           JavaMailEmailService javaMailEmailService) {
        this.userRepository = userRepository;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
        this.javaMailEmailService = javaMailEmailService;
    }

    @Override
    @Transactional
    public void handle(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        if (userOptional.isEmpty()) {
            return;
        }
        User user = userOptional.get();

        if (user.isEmailVerified()) {
            return;
        }

        emailConfirmationTokenRepository.deleteByUser(user);

        String plainTextToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(plainTextToken);

        EmailConfirmationToken myToken = new EmailConfirmationToken(tokenHash, user, LocalDateTime.now().plusHours(24));
        emailConfirmationTokenRepository.save(myToken);

        String htmlContent = getConfirmationEmailHtml(plainTextToken);
        javaMailEmailService.sendHtml(user.getEmail(), "Confirme seu E-mail - Projeto Resgate", htmlContent);
    }

    private String getConfirmationEmailHtml(String token) {
        String confirmationUrl = "http://localhost:3000/confirm-email/" + token;

        return """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <style>
                      body { font-family: 'Arial', sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }
                      .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                      .header { font-size: 24px; font-weight: bold; color: #16A34A; text-align: center; margin-bottom: 20px; }
                      .content { font-size: 16px; color: #333; line-height: 1.5; }
                      .button { display: block; width: 200px; margin: 20px auto; padding: 15px; background-color: #16A34A; color: #ffffff; text-align: center; text-decoration: none; border-radius: 5px; font-weight: bold; }
                      .footer { font-size: 12px; color: #777; text-align: center; margin-top: 20px; }
                    </style>
                  </head>
                  <body>
                    <div class="container">
                      <div class="header">Confirme seu E-mail</div>
                      <div class="content">
                        <p>Olá,</p>
                        <p>Para completar seu cadastro no <strong>Projeto Resgate</strong>, clique no botão abaixo para confirmar seu endereço de e-mail.</p>
                        <a href="%s" class="button" style="color: #ffffff;">Confirmar E-mail</a>
                        <p>Se você não criou esta conta, pode ignorar este e-mail com segurança.</p>
                      </div>
                      <div class="footer">
                        Este é um e-mail automático. Não é necessário respondê-lo.
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(confirmationUrl);
    }
}
