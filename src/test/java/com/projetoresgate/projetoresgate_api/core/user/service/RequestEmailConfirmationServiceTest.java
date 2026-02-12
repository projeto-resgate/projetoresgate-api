package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestEmailConfirmationService - Test")
class RequestEmailConfirmationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    @Mock
    private JavaMailEmailService javaMailEmailService;

    @InjectMocks
    private RequestEmailConfirmationService requestEmailConfirmationService;

    private User existingUser;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "test@example.com";
        existingUser = new User(userEmail, "password", "Test User");
        existingUser.setId(UUID.randomUUID());
        existingUser.setIsEmailVerified(false);
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail do usuário não existir")
    void handle_shouldDoNothing_whenUserEmailDoesNotExist() {
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.empty());

        requestEmailConfirmationService.handle(userEmail);

        verifyNoInteractions(emailConfirmationTokenRepository, javaMailEmailService);
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail do usuário já estiver verificado")
    void handle_shouldDoNothing_whenUserEmailIsAlreadyVerified() {
        existingUser.setIsEmailVerified(true);
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(existingUser));

        requestEmailConfirmationService.handle(userEmail);

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

            when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(existingUser));

            ArgumentCaptor<EmailConfirmationToken> tokenCaptor = ArgumentCaptor.forClass(EmailConfirmationToken.class);
            ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

            requestEmailConfirmationService.handle(userEmail);

            verify(emailConfirmationTokenRepository).deleteByUser(existingUser);
            verify(emailConfirmationTokenRepository).save(tokenCaptor.capture());
            verify(javaMailEmailService).sendHtml(eq(userEmail), anyString(), htmlCaptor.capture());

            EmailConfirmationToken savedToken = tokenCaptor.getValue();
            String emailHtml = htmlCaptor.getValue();

            assertEquals(expectedTokenHash, savedToken.getTokenHash());
            assertTrue(emailHtml.contains("/confirm-email/" + plainTextToken));
        }
    }
}
