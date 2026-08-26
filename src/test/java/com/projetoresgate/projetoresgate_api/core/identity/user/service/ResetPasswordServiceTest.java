package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.PasswordResetToken;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.PasswordResetTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de Redefinição de Senha - Test")
class ResetPasswordServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordService resetPasswordService;

    private User user;
    private PasswordResetToken resetToken;
    private String plainTextToken;
    private String newPassword;

    @BeforeEach
    void setUp() {
        user = User.create("test@example.com", "oldPassword", "Test User", "tester");
        plainTextToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(plainTextToken);
        newPassword = "newStrongPassword";
        resetToken = new PasswordResetToken(tokenHash, user, LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Deve redefinir a senha com sucesso com um token válido")
    void handle_shouldResetPassword_withValidToken() {
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(passwordResetTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        resetPasswordService.handle(plainTextToken, newPassword);

        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o token não for encontrado")
    void handle_shouldThrowException_whenTokenNotFound() {
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(passwordResetTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> {
            resetPasswordService.handle(plainTextToken, newPassword);
        });

        assertEquals("Token inválido ou não encontrado.", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção e deletar o token quando estiver expirado")
    void handle_shouldThrowExceptionAndDeletesToken_whenTokenIsExpired() {
        String tokenHash = TokenUtils.hashToken(plainTextToken);
        PasswordResetToken expiredToken = new PasswordResetToken(tokenHash, user, LocalDateTime.now().minusMinutes(1));
        when(passwordResetTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

        InternalException exception = assertThrows(InternalException.class, () -> {
            resetPasswordService.handle(plainTextToken, newPassword);
        });

        assertEquals("O token expirou. Solicite uma nova redefinição de senha.", exception.getMessage());
        verify(passwordResetTokenRepository).delete(expiredToken);
        verify(userRepository, never()).save(any());
    }
}