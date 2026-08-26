package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.ConfirmEmailCommand;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmEmailService - Test")
class ConfirmEmailServiceTest {

    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private ConfirmEmailService confirmEmailService;

    private NaturalPerson person;
    private EmailConfirmationToken confirmationToken;
    private String plainTextToken;

    @BeforeEach
    void setUp() {
        person = NaturalPerson.create("Test Person", "test@example.com", "tester", "51086174968", null, null, null, null, null);

        plainTextToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(plainTextToken);

        confirmationToken = new EmailConfirmationToken(tokenHash, person, LocalDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Deve confirmar o e-mail com sucesso com um token válido")
    void handle_shouldConfirmEmail_withValidToken() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(emailConfirmationTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(confirmationToken));

        confirmEmailService.handle(command);

        assertTrue(person.isEmailVerified());
        verify(naturalPersonRepository).save(person);
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
        verify(naturalPersonRepository, never()).save(any());
        verify(emailConfirmationTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o token está expirado")
    void handle_shouldThrowException_whenTokenIsExpired() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        String tokenHash = TokenUtils.hashToken(plainTextToken);
        EmailConfirmationToken expiredToken = new EmailConfirmationToken(tokenHash, person, LocalDateTime.now().minusHours(1));
        when(emailConfirmationTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

        InternalException exception = assertThrows(InternalException.class, () -> confirmEmailService.handle(command));
        assertEquals("O token expirou. Solicite um novo.", exception.getMessage());
        verify(naturalPersonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail já estiver verificado")
    void handle_shouldDoNothing_whenEmailIsAlreadyVerified() {
        ConfirmEmailCommand command = new ConfirmEmailCommand(plainTextToken);
        person.confirmEmail();
        String expectedHash = TokenUtils.hashToken(plainTextToken);
        when(emailConfirmationTokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.of(confirmationToken));

        confirmEmailService.handle(command);

        verify(naturalPersonRepository, never()).save(any());
        verify(emailConfirmationTokenRepository, never()).delete(any());
    }
}
