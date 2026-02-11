package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.EmailConfirmationToken;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.email.JavaMailEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @DisplayName("Deve criar token e enviar e-mail se o usuário existir e não estiver verificado")
    void handle_shouldCreateTokenAndSendEmail_whenUserExistsAndNotVerified() {
        when(userRepository.findUserByEmail(userEmail)).thenReturn(Optional.of(existingUser));

        requestEmailConfirmationService.handle(userEmail);

        verify(emailConfirmationTokenRepository).deleteByUser(existingUser);

        ArgumentCaptor<EmailConfirmationToken> tokenCaptor = ArgumentCaptor.forClass(EmailConfirmationToken.class);
        verify(emailConfirmationTokenRepository).save(tokenCaptor.capture());
        EmailConfirmationToken savedToken = tokenCaptor.getValue();
        assertEquals(existingUser, savedToken.getUser());
        assertTrue(savedToken.getToken().matches("\\d{6}"), "O token deve ser um número de 6 dígitos");

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);
        verify(javaMailEmailService).sendHtml(eq(userEmail), eq("Código de Confirmação - Projeto Resgate"), htmlCaptor.capture());
        String emailHtml = htmlCaptor.getValue();
        assertTrue(emailHtml.contains(savedToken.getToken()));
    }
}
