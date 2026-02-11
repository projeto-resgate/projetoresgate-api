package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
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

        SecureRandom random = new SecureRandom();
        String token = String.format("%06d", random.nextInt(1_000_000));

        EmailConfirmationToken myToken = new EmailConfirmationToken(token, user, LocalDateTime.now().plusHours(24));
        emailConfirmationTokenRepository.save(myToken);

        String htmlContent = getConfirmationEmailHtml(token);
        javaMailEmailService.sendHtml(user.getEmail(), "Código de Confirmação - Projeto Resgate", htmlContent);
    }

    private String getConfirmationEmailHtml(String token) {
        return """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <style>
                      body {
                        margin: 0;
                        padding: 0;
                        background-color: #F3F4F6;
                        font-family: 'Arial', sans-serif;
                      }
                      .container {
                        background-color: #FFFFFF;
                        color: #1F2937;
                        max-width: 500px;
                        margin: 40px auto;
                        border-radius: 10px;
                        padding: 40px 30px;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                        border-top: 5px solid #16A34A;
                      }
                      .title {
                        font-size: 24px;
                        font-weight: bold;
                        text-align: center;
                        margin-bottom: 25px;
                        color: #16A34A;
                      }
                      .content {
                        font-size: 16px;
                        line-height: 1.6;
                        color: #374151;
                      }
                      .code-container {
                        text-align: center;
                        margin: 30px 0;
                      }
                      .code {
                        background-color: #DCFCE7;
                        color: #15803D;
                        padding: 15px 30px;
                        border-radius: 8px;
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 5px;
                        display: inline-block;
                        border: 1px solid #86EFAC;
                      }
                      .footer {
                        font-size: 12px;
                        text-align: center;
                        color: #9CA3AF;
                        margin-top: 30px;
                      }
                    </style>
                  </head>
                  <body>
                    <div class="container">
                      <div class="title">Confirme seu E-mail</div>
                      <div class="content">
                        <p>Olá,</p>
                        <p>Use o código abaixo para confirmar seu endereço de e-mail e ativar sua conta no <strong>Projeto Resgate</strong>:</p>
                
                        <div class="code-container">
                          <div class="code">%s</div>
                        </div>
                
                        <p>Copie este código e cole no aplicativo.</p>
                        <p>Se você não solicitou este código, pode ignorar este e-mail.</p>
                      </div>
                
                      <div class="footer">
                        Este é um e-mail automático. Não é necessário respondê-lo.
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(token);
    }
}
