package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.ConfirmEmailCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmEmailService - Test")
class ConfirmEmailServiceTest {

    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConfirmEmailService confirmEmailService;

    private User user;
    private EmailConfirmationToken confirmationToken;
    private String plainTextToken;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "password", "Test User");
        user.setId(UUID.randomUUID());
        user.setIsEmailVerified(false);

        plainTextToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(plainTextToken);

        confirmationToken = new EmailConfirmationToken(tokenHash, user, LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Deve confirmar o e-mail com sucesso com um token válido")
    void handle_shouldConfirmEmail_withValidToken() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(emailConfirmationTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(confirmationToken));

        confirmEmailService.handle(command);

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
        verify(emailConfirmationTokenRepository).delete(confirmationToken);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o token não for encontrado")
    void handle_shouldThrowException_whenTokenNotFound() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(emailConfirmationTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> confirmEmailService.handle(command));
        assertEquals("Token inválido ou não encontrado.", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(emailConfirmationTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o token está expirado")
    void handle_shouldThrowException_whenTokenIsExpired() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        confirmationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(emailConfirmationTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(confirmationToken));

        InternalException exception = assertThrows(InternalException.class, () -> confirmEmailService.handle(command));
        assertEquals("O token expirou. Solicite um novo.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail já estiver verificado")
    void handle_shouldDoNothing_whenEmailIsAlreadyVerified() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        user.setIsEmailVerified(true);
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(emailConfirmationTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(confirmationToken));

        confirmEmailService.handle(command);

        verify(userRepository, never()).save(any());
        verify(emailConfirmationTokenRepository, never()).delete(any());
    }
}
