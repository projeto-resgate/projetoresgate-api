package com.projetoresgate.projetoresgate_api.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailRateLimiter - Test")
class EmailRateLimiterTest {

    @Test
    @DisplayName("Deve permitir até o limite por destinatário e bloquear depois")
    void shouldBlockAfterLimit() {
        EmailRateLimiter limiter = new EmailRateLimiter(true, 3, 3600);

        assertTrue(limiter.tryAcquire("ana@test.com"));
        assertTrue(limiter.tryAcquire("ana@test.com"));
        assertTrue(limiter.tryAcquire("ana@test.com"));
        assertFalse(limiter.tryAcquire("ana@test.com"));
    }

    @Test
    @DisplayName("Deve tratar o mesmo e-mail escrito com maiúsculas e espaços como um só destinatário")
    void shouldNormalizeEmail() {
        EmailRateLimiter limiter = new EmailRateLimiter(true, 1, 3600);

        assertTrue(limiter.tryAcquire("Ana@Test.com"));
        assertFalse(limiter.tryAcquire("  ana@test.COM "));
    }

    @Test
    @DisplayName("Destinatários diferentes devem ter limites independentes")
    void shouldKeepRecipientsIndependent() {
        EmailRateLimiter limiter = new EmailRateLimiter(true, 1, 3600);

        assertTrue(limiter.tryAcquire("a@test.com"));
        assertFalse(limiter.tryAcquire("a@test.com"));
        assertTrue(limiter.tryAcquire("b@test.com"));
    }

    @Test
    @DisplayName("Desligado, deve sempre permitir")
    void disabled_shouldAlwaysAllow() {
        EmailRateLimiter limiter = new EmailRateLimiter(false, 1, 3600);

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.tryAcquire("a@test.com"));
        }
    }

    @Test
    @DisplayName("E-mail vazio ou nulo não deve ser limitado (quem chama já trata o caso)")
    void blankEmail_shouldNotBeLimited() {
        EmailRateLimiter limiter = new EmailRateLimiter(true, 1, 3600);

        assertTrue(limiter.tryAcquire(null));
        assertTrue(limiter.tryAcquire(" "));
        assertTrue(limiter.tryAcquire(null));
    }
}
