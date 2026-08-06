package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
