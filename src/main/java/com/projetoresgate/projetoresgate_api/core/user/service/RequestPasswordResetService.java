package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.PasswordResetToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.PasswordResetTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.RequestPasswordResetUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailEmailService javaMailEmailService;

    public RequestPasswordResetService(UserRepository userRepository,
                                       PasswordResetTokenRepository passwordResetTokenRepository,
                                       JavaMailEmailService javaMailEmailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
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

        passwordResetTokenRepository.deleteByUser(user);

        String plainTextToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(plainTextToken);

        PasswordResetToken myToken = new PasswordResetToken(tokenHash, user, LocalDateTime.now().plusMinutes(10));
        passwordResetTokenRepository.save(myToken);

        String htmlContent = getResetPasswordHtml(plainTextToken);
        javaMailEmailService.sendHtml(user.getEmail(), "Redefinição de Senha - Projeto Resgate", htmlContent);
    }

    private String getResetPasswordHtml(String token) {
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
                      .btn-container {
                        text-align: center;
                        margin: 30px 0;
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
                      <div class="title">Redefinição de Senha</div>
                      <div class="content">
                        <p>Olá,</p>
                        <p>Recebemos uma solicitação para redefinir sua senha no <strong>Projeto Resgate</strong>.</p>
                        <p>Clique no botão abaixo para continuar:</p>
                
                        <div class="btn-container">
                          <a href="http://localhost:5173/reset-password?token=%s"
                             style="background-color: #16A34A; color: #FFFFFF; text-decoration: none; padding: 14px 28px; border-radius: 6px; font-size: 16px; font-weight: bold; display: inline-block;">
                            Redefinir Senha
                          </a>
                        </div>
                
                        <p>Se você não solicitou essa redefinição, pode ignorar este e-mail.</p>
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
