package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import com.projetoresgate.projetoresgate_api.infrastructure.security.EmailRateLimiter;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestEmailConfirmationService - Test")
class RequestEmailConfirmationServiceTest {

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Mock
    private JavaMailEmailService javaMailEmailService;

    @Mock
    private EmailRateLimiter emailRateLimiter;

    @InjectMocks
    private RequestEmailConfirmationService requestEmailConfirmationService;

    private NaturalPerson existingPerson;
    private String personEmail;

    @BeforeEach
    void setUp() {
        personEmail = "test@example.com";
        existingPerson = NaturalPerson.create("Test Person", personEmail, "tester", "51086174968", null, null, null, null, null);
        lenient().when(emailRateLimiter.tryAcquire(anyString())).thenReturn(true);
        ReflectionTestUtils.setField(requestEmailConfirmationService, "frontendUrl", "https://app.exemplo.org/");
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail da pessoa física não existir")
    void handle_shouldDoNothing_whenEmailDoesNotExist() {
        when(naturalPersonRepository.findAllByEmailIgnoreCase(personEmail)).thenReturn(List.of());

        requestEmailConfirmationService.handle(personEmail);

        verifyNoInteractions(emailConfirmationTokenRepository, javaMailEmailService);
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail da pessoa física já estiver verificado")
    void handle_shouldDoNothing_whenEmailIsAlreadyVerified() {
        existingPerson.confirmEmail();
        when(naturalPersonRepository.findAllByEmailIgnoreCase(personEmail)).thenReturn(List.of(existingPerson));

        requestEmailConfirmationService.handle(personEmail);

        verifyNoInteractions(emailConfirmationTokenRepository, javaMailEmailService);
    }

    @Test
    @DisplayName("Deve criar token com hash e enviar e-mail com token original")
    void handle_shouldCreateHashedTokenAndSendEmailWithPlainText() {
        String plainTextToken = "my-secure-plain-text-token";
        String expectedTokenHash = "expected-hash-of-the-token";

        try (MockedStatic<TokenUtils> mockedTokenUtils = Mockito.mockStatic(TokenUtils.class)) {
            mockedTokenUtils.when(TokenUtils::generateSecureToken).thenReturn(plainTextToken);
            mockedTokenUtils.when(() -> TokenUtils.hashToken(plainTextToken)).thenReturn(expectedTokenHash);

            when(naturalPersonRepository.findAllByEmailIgnoreCase(personEmail)).thenReturn(List.of(existingPerson));

            ArgumentCaptor<EmailConfirmationToken> tokenCaptor = ArgumentCaptor.forClass(EmailConfirmationToken.class);
            ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

            requestEmailConfirmationService.handle(personEmail);

            verify(emailConfirmationTokenRepository).deleteByNaturalPerson(existingPerson);
            verify(emailConfirmationTokenRepository).save(tokenCaptor.capture());
            verify(javaMailEmailService).sendHtml(eq(personEmail), anyString(), htmlCaptor.capture());

            EmailConfirmationToken savedToken = tokenCaptor.getValue();
            String emailHtml = htmlCaptor.getValue();

            assertEquals(expectedTokenHash, savedToken.getTokenHash());
            assertEquals(existingPerson, savedToken.getNaturalPerson());
            assertTrue(emailHtml.contains("href=\"https://app.exemplo.org/confirm-email/" + plainTextToken + "\""));
        }
    }

    @Test
    @DisplayName("Deve ignorar em silêncio quando o destinatário atingiu o limite de e-mails")
    void handle_shouldDoNothing_whenEmailRateLimitExceeded() {
        when(emailRateLimiter.tryAcquire(personEmail)).thenReturn(false);

        requestEmailConfirmationService.handle(personEmail);

        verifyNoInteractions(naturalPersonRepository, emailConfirmationTokenRepository, javaMailEmailService);
    }

    @Test
    @DisplayName("Deve enviar confirmação a cada pessoa não verificada que compartilha o e-mail")
    void handle_shouldSendToEveryUnverifiedPersonSharingTheEmail() {
        NaturalPerson child = NaturalPerson.create("Criança", personEmail, null, null, null, null, null, null, null);
        NaturalPerson verified = NaturalPerson.create("Responsável", personEmail, null, null, null, null, null, null, null);
        verified.confirmEmail();
        when(naturalPersonRepository.findAllByEmailIgnoreCase(personEmail)).thenReturn(List.of(existingPerson, child, verified));

        requestEmailConfirmationService.handle(personEmail);

        verify(emailConfirmationTokenRepository).deleteByNaturalPerson(existingPerson);
        verify(emailConfirmationTokenRepository).deleteByNaturalPerson(child);
        verify(emailConfirmationTokenRepository, never()).deleteByNaturalPerson(verified);
        verify(javaMailEmailService, times(2)).sendHtml(eq(personEmail), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve enviar confirmação só para a pessoa informada")
    void handlePerson_shouldSendOnlyToGivenPerson() {
        requestEmailConfirmationService.handle(existingPerson);

        verify(emailConfirmationTokenRepository).deleteByNaturalPerson(existingPerson);
        verify(javaMailEmailService).sendHtml(eq(personEmail), anyString(), anyString());
        verifyNoInteractions(naturalPersonRepository);
    }

    @Test
    @DisplayName("Não deve enviar confirmação para pessoa já verificada")
    void handlePerson_shouldSkipVerifiedPerson() {
        existingPerson.confirmEmail();

        requestEmailConfirmationService.handle(existingPerson);

        verifyNoInteractions(emailConfirmationTokenRepository, javaMailEmailService);
    }
}
