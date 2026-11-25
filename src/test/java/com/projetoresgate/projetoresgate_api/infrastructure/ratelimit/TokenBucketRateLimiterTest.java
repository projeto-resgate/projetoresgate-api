package com.projetoresgate.projetoresgate_api.infrastructure.ratelimit;

import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter.Decision;
import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenBucketRateLimiter - Test")
class TokenBucketRateLimiterTest {

    private static final long SECOND = 1_000_000_000L;

    private final AtomicLong clock = new AtomicLong(0);
    private TokenBucketRateLimiter limiter;
    private final Policy fivePerMinute = new Policy(5, Duration.ofSeconds(60));

    @BeforeEach
    void setUp() {
        limiter = new TokenBucketRateLimiter(clock::get);
    }

    @Test
    @DisplayName("Deve permitir até a capacidade e bloquear a seguinte")
    void shouldAllowUpToCapacityThenReject() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire("k", fivePerMinute).allowed(), "tentativa " + (i + 1));
        }
        Decision rejected = limiter.tryAcquire("k", fivePerMinute);

        assertFalse(rejected.allowed());
        assertEquals(12, rejected.retryAfterSeconds());
    }

    @Test
    @DisplayName("Deve reabastecer continuamente, sem esperar a janela inteira")
    void shouldRefillGradually() {
        for (int i = 0; i < 5; i++) limiter.tryAcquire("k", fivePerMinute);
        assertFalse(limiter.tryAcquire("k", fivePerMinute).allowed());

        clock.addAndGet(12 * SECOND);
        assertTrue(limiter.tryAcquire("k", fivePerMinute).allowed(), "uma ficha voltou após 12 s");
        assertFalse(limiter.tryAcquire("k", fivePerMinute).allowed(), "mas só uma");
    }

    @Test
    @DisplayName("Não deve acumular mais fichas que a capacidade, por mais que o tempo passe")
    void shouldNotExceedCapacityAfterLongIdle() {
        limiter.tryAcquire("k", fivePerMinute);
        clock.addAndGet(3600 * SECOND);

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire("k", fivePerMinute).allowed());
        }
        assertFalse(limiter.tryAcquire("k", fivePerMinute).allowed());
    }

    @Test
    @DisplayName("Chaves diferentes devem ter baldes independentes")
    void shouldKeepKeysIndependent() {
        for (int i = 0; i < 5; i++) limiter.tryAcquire("a", fivePerMinute);

        assertFalse(limiter.tryAcquire("a", fivePerMinute).allowed());
        assertTrue(limiter.tryAcquire("b", fivePerMinute).allowed());
    }

    @Test
    @DisplayName("Deve informar um retry-after coerente com o tempo que falta")
    void shouldReportRemainingWait() {
        for (int i = 0; i < 5; i++) limiter.tryAcquire("k", fivePerMinute);
        clock.addAndGet(5 * SECOND);

        assertEquals(7, limiter.tryAcquire("k", fivePerMinute).retryAfterSeconds());
    }

    @Test
    @DisplayName("Deve remover baldes ociosos para não vazar memória")
    void shouldCleanUpIdleBuckets() {
        limiter.tryAcquire("a", fivePerMinute);
        limiter.tryAcquire("b", fivePerMinute);
        assertEquals(2, limiter.size());

        clock.addAndGet(61 * SECOND);
        limiter.cleanup(clock.get());

        assertEquals(0, limiter.size());
    }

    @Test
    @DisplayName("Não deve remover baldes ainda em uso")
    void shouldKeepActiveBucketsOnCleanup() {
        limiter.tryAcquire("a", fivePerMinute);
        clock.addAndGet(30 * SECOND);
        limiter.cleanup(clock.get());

        assertEquals(1, limiter.size());
    }

    @Test
    @DisplayName("Deve rejeitar política inválida")
    void shouldRejectInvalidPolicy() {
        assertThrows(IllegalArgumentException.class, () -> new Policy(0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new Policy(1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Policy(1, null));
    }

    @Test
    @DisplayName("Acima do teto de chaves, chaves novas passam (fail-open) e o mapa não cresce")
    void shouldFailOpenAboveKeyCap() {
        for (int i = 0; i < TokenBucketRateLimiter.MAX_KEYS; i++) {
            limiter.tryAcquire("k" + i, fivePerMinute);
        }
        assertEquals(TokenBucketRateLimiter.MAX_KEYS, limiter.size());

        for (int i = 0; i < 20; i++) {
            assertTrue(limiter.tryAcquire("novo", fivePerMinute).allowed());
        }
        assertEquals(TokenBucketRateLimiter.MAX_KEYS, limiter.size());
    }
}
