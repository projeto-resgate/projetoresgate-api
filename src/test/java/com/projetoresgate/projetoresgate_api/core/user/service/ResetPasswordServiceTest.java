package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.PasswordResetToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.PasswordResetTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
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
import java.util.UUID;

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
    private String token;
    private String newPassword;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "oldPassword", "Test User");
        user.setId(UUID.randomUUID());

        token = UUID.randomUUID().toString();
        newPassword = "newStrongPassword";

        resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Deve redefinir a senha com sucesso com um token válido")
    void handle_shouldResetPassword_withValidToken() {
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        resetPasswordService.handle(token, newPassword);

        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
        verify(passwordResetTokenRepository).delete(resetToken);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o token não for encontrado")
    void handle_shouldThrowException_whenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> {
            resetPasswordService.handle(token, newPassword);
        });

        assertEquals("Token inválido ou não encontrado.", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção e deletar o token quando estiver expirado")
    void handle_shouldThrowExceptionAndDeletesToken_whenTokenIsExpired() {
        resetToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        InternalException exception = assertThrows(InternalException.class, () -> {
            resetPasswordService.handle(token, newPassword);
        });

        assertEquals("O token expirou. Solicite uma nova redefinição de senha.", exception.getMessage());
        verify(passwordResetTokenRepository).delete(resetToken);
        verify(userRepository, never()).save(any());
    }
}
