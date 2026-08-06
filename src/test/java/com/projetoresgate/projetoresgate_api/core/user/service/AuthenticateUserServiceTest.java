package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.service.AuthenticateUserService;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateUserService - Test")
class AuthenticateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ITokenService tokenService;

    @Mock
    private IRefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticateUserService authenticateUserService;

    private AuthenticateUserQuery authQuery;
    private User existingUser;

    @BeforeEach
    void setUp() {
        authQuery = new AuthenticateUserQuery("test@example.com", "password123");
        existingUser = User.create("test@example.com", "encodedPassword", "Test User", "tester");
    }

    @Test
    @DisplayName("Deve retornar AuthenticationResponse em autenticação bem-sucedida")
    void handle_shouldReturnResponse_onSuccessfulAuthentication() {
        when(userRepository.findByEmail(authQuery.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(authQuery.password(), existingUser.getPassword())).thenReturn(true);
        when(tokenService.generateAccessToken(existingUser)).thenReturn("mocked.jwt.token");
        when(refreshTokenService.createRefreshToken(existingUser)).thenReturn("mocked.refresh.token");

        AuthenticationResponse response = authenticateUserService.handle(authQuery);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.accessToken());
        assertEquals("mocked.refresh.token", response.refreshToken());
        assertEquals(existingUser.getId().toString(), response.userId());
        assertEquals(existingUser.getName(), response.name());
        assertEquals(existingUser.getRoles(), response.roles());

        verify(userRepository).findByEmail(authQuery.email());
        verify(passwordEncoder).matches(authQuery.password(), existingUser.getPassword());
        verify(tokenService).generateAccessToken(existingUser);
        verify(refreshTokenService).createRefreshToken(existingUser);
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando usuário não encontrado")
    void handle_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail(authQuery.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> {
            authenticateUserService.handle(authQuery);
        });

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    @DisplayName("Deve lançar BadCredentialsException quando a senha não corresponde")
    void handle_shouldThrowException_whenPasswordDoesNotMatch() {
        when(userRepository.findByEmail(authQuery.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(authQuery.password(), existingUser.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            authenticateUserService.handle(authQuery);
        });

        verify(tokenService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }
}