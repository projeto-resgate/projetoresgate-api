package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

    @InjectMocks
    private RequestEmailConfirmationService requestEmailConfirmationService;

    private NaturalPerson existingPerson;
    private String personEmail;

    @BeforeEach
    void setUp() {
        personEmail = "test@example.com";
        existingPerson = NaturalPerson.create("Test Person", personEmail, "tester", "51086174968", null, null, null, null, null);
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail da pessoa física não existir")
    void handle_shouldDoNothing_whenEmailDoesNotExist() {
        when(naturalPersonRepository.findByEmail(personEmail)).thenReturn(Optional.empty());

        requestEmailConfirmationService.handle(personEmail);

        verifyNoInteractions(emailConfirmationTokenRepository, javaMailEmailService);
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail da pessoa física já estiver verificado")
    void handle_shouldDoNothing_whenEmailIsAlreadyVerified() {
        existingPerson.confirmEmail();
        when(naturalPersonRepository.findByEmail(personEmail)).thenReturn(Optional.of(existingPerson));

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

            when(naturalPersonRepository.findByEmail(personEmail)).thenReturn(Optional.of(existingPerson));

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
            assertTrue(emailHtml.contains("/confirm-email/" + plainTextToken));
        }
    }
}
