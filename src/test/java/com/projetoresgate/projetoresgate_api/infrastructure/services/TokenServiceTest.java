package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenService - Test")
class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret");
        ReflectionTestUtils.setField(tokenService, "accessTokenDuration", 900L);
    }

    @Test
    @DisplayName("Deve gerar access token com a versão atual do usuário")
    void generateAccessToken_ShouldIncludeTokenVersion() {
        User user = User.create("test@example.com", "encoded", "Test User", "tester");

        String token = tokenService.generateAccessToken(user);

        assertEquals(user.getId().toString(), tokenService.validateAccessToken(token));
        assertEquals(user.getTokenVersion(), tokenService.getTokenVersion(token));
    }

    @Test
    @DisplayName("Token antigo deve ficar defasado quando a versão do usuário for incrementada")
    void getTokenVersion_ShouldBecomeStaleAfterInvalidation() {
        User user = User.create("test@example.com", "encoded", "Test User", "tester");
        String token = tokenService.generateAccessToken(user);

        user.invalidateTokens();

        assertNotEquals(user.getTokenVersion(), tokenService.getTokenVersion(token));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o token for inválido")
    void getTokenVersion_ShouldThrowOnInvalidToken() {
        assertThrows(InternalException.class, () -> tokenService.getTokenVersion("invalid-token"));
    }

    @Test
    @DisplayName("Access token deve expirar em 15 min independente do fuso horário da JVM (regressão: container em UTC)")
    void generateAccessToken_ShouldExpireInConfiguredDuration_regardlessOfTimeZone() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            User user = User.create("test@example.com", "encoded", "Test User", "tester");

            Instant before = Instant.now();
            String token = tokenService.generateAccessToken(user);
            Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();

            Duration lifetime = Duration.between(before, expiresAt);
            assertTrue(lifetime.compareTo(Duration.ofSeconds(895)) >= 0, "expira cedo demais: " + lifetime);
            assertTrue(lifetime.compareTo(Duration.ofSeconds(905)) <= 0, "expira tarde demais: " + lifetime);
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
