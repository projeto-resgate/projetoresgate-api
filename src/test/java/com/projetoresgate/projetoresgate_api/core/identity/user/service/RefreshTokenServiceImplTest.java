package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.RefreshTokenResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.RefreshTokenQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenServiceImpl - Test")
class RefreshTokenServiceImplTest {

    @Mock
    private IRefreshTokenService refreshTokenService;

    @Mock
    private ITokenService tokenService;

    @InjectMocks
    private RefreshTokenServiceImpl sut;

    private User user;
    private String oldRefreshToken;
    private RefreshTokenQuery query;

    @BeforeEach
    void setUp() {
        user = User.create("test@example.com", "encodedPass", "Test User", "tester");
        oldRefreshToken = "valid-refresh-token";
        query = new RefreshTokenQuery(oldRefreshToken);
    }

    @Test
    @DisplayName("Deve retornar novos tokens quando refresh token é válido")
    void handle_shouldReturnNewTokens_whenValidToken() {
        when(refreshTokenService.validateRefreshToken(oldRefreshToken)).thenReturn(user);
        when(tokenService.generateAccessToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn("new-refresh-token");
        when(tokenService.getAccessTokenDurationSeconds()).thenReturn(900L);

        RefreshTokenResponse response = sut.handle(query);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        assertEquals(900L, response.expiresIn());
        assertEquals("Bearer", response.tokenType());

        verify(refreshTokenService).validateRefreshToken(oldRefreshToken);
        verify(tokenService).generateAccessToken(user);
        verify(refreshTokenService).createRefreshToken(user);
        verify(refreshTokenService).revokeRefreshToken(oldRefreshToken);
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token é inválido")
    void handle_shouldThrow_whenInvalidToken() {
        when(refreshTokenService.validateRefreshToken(oldRefreshToken))
                .thenThrow(new InternalException("Token de renovação inválido ou não encontrado."));

        InternalException exception = assertThrows(InternalException.class, () -> sut.handle(query));
        assertEquals("Token de renovação inválido ou não encontrado.", exception.getMessage());

        verify(tokenService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
        verify(refreshTokenService, never()).revokeRefreshToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token está expirado")
    void handle_shouldThrow_whenExpiredToken() {
        when(refreshTokenService.validateRefreshToken(oldRefreshToken))
                .thenThrow(new InternalException("Token de renovação expirou."));

        InternalException exception = assertThrows(InternalException.class, () -> sut.handle(query));
        assertEquals("Token de renovação expirou.", exception.getMessage());

        verify(tokenService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
        verify(refreshTokenService, never()).revokeRefreshToken(any());
    }
}
