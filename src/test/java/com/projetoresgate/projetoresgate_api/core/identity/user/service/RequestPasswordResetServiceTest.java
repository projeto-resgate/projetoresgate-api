package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.PasswordResetToken;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.PasswordResetTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestPasswordResetService - Test")
class RequestPasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JavaMailEmailService javaMailEmailService;

    @Mock
    private EmailRateLimiter emailRateLimiter;

    @InjectMocks
    private RequestPasswordResetService requestPasswordResetService;

    private User existingUser;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userEmail = "test@example.com";
        existingUser = User.create(userEmail, "password", "Test User", "tester");
        lenient().when(emailRateLimiter.tryAcquire(anyString())).thenReturn(true);
        ReflectionTestUtils.setField(requestPasswordResetService, "frontendUrl", "https://app.exemplo.org/");
    }

    @Test
    @DisplayName("Não deve fazer nada se o e-mail do usuário não existir")
    void handle_shouldDoNothing_whenUserEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.empty());

        requestPasswordResetService.handle(userEmail);

        verifyNoInteractions(passwordResetTokenRepository, javaMailEmailService);
    }

    @Test
    @DisplayName("Deve criar token com hash e enviar e-mail com token original")
    void handle_shouldCreateTokenAndSendEmail_whenUserExists() {
        String plainTextToken = "my-secure-plain-text-token";
        String expectedTokenHash = "expected-hash-of-the-token";

        try (MockedStatic<TokenUtils> mockedTokenUtils = Mockito.mockStatic(TokenUtils.class)) {
            mockedTokenUtils.when(TokenUtils::generateSecureToken).thenReturn(plainTextToken);
            mockedTokenUtils.when(() -> TokenUtils.hashToken(plainTextToken)).thenReturn(expectedTokenHash);

            when(userRepository.findByEmailIgnoreCase(userEmail)).thenReturn(Optional.of(existingUser));

            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

            requestPasswordResetService.handle(userEmail);

            verify(passwordResetTokenRepository).deleteByUser(existingUser);
            verify(passwordResetTokenRepository).save(tokenCaptor.capture());
            verify(javaMailEmailService).sendHtml(eq(userEmail), anyString(), htmlCaptor.capture());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            String emailHtml = htmlCaptor.getValue();

            assertEquals(expectedTokenHash, savedToken.getTokenHash());
            assertTrue(emailHtml.contains("href=\"https://app.exemplo.org/reset-password?token=" + plainTextToken + "\""));
        }
    }

    @Test
    @DisplayName("Deve ignorar em silêncio quando o destinatário atingiu o limite de e-mails")
    void handle_shouldDoNothing_whenEmailRateLimitExceeded() {
        when(emailRateLimiter.tryAcquire(userEmail)).thenReturn(false);

        requestPasswordResetService.handle(userEmail);

        verifyNoInteractions(userRepository, passwordResetTokenRepository, javaMailEmailService);
    }
}
