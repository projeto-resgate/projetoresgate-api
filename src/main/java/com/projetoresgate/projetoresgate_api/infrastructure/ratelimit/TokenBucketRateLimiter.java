package com.projetoresgate.projetoresgate_api.infrastructure.ratelimit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

public final class TokenBucketRateLimiter {

    public record Policy(int maxRequests, Duration window) {
        public Policy {
            if (maxRequests < 1) {
                throw new IllegalArgumentException("maxRequests deve ser >= 1");
            }
            if (window == null || window.isZero() || window.isNegative()) {
                throw new IllegalArgumentException("window deve ser positiva");
            }
        }
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {
        static final Decision PERMITTED = new Decision(true, 0);
    }

    static final int MAX_KEYS = 100_000;
    private static final int CLEANUP_EVERY_N_CALLS = 1_000;
    private static final double NANOS_PER_SECOND = 1_000_000_000d;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();
    private final LongSupplier nanoClock;

    public TokenBucketRateLimiter() {
        this(System::nanoTime);
    }

    public TokenBucketRateLimiter(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    public Decision tryAcquire(String key, Policy policy) {
        long now = nanoClock.getAsLong();
        if (calls.incrementAndGet() % CLEANUP_EVERY_N_CALLS == 0 || buckets.size() >= MAX_KEYS) {
            cleanup(now);
        }

        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            if (buckets.size() >= MAX_KEYS) {
                return Decision.PERMITTED;
            }
            bucket = buckets.computeIfAbsent(key, k -> new Bucket(policy, now));
        }
        return bucket.tryConsume(now);
    }

    int size() {
        return buckets.size();
    }

    void cleanup(long nowNanos) {
        buckets.entrySet().removeIf(entry -> entry.getValue().isIdle(nowNanos));
    }

    private static final class Bucket {
        private final double capacity;
        private final double refillPerNano;
        private final long windowNanos;
        private double tokens;
        private long lastNanos;

        Bucket(Policy policy, long nowNanos) {
            this.capacity = policy.maxRequests();
            this.windowNanos = policy.window().toNanos();
            this.refillPerNano = capacity / windowNanos;
            this.tokens = capacity;
            this.lastNanos = nowNanos;
        }

        synchronized Decision tryConsume(long nowNanos) {
            refill(nowNanos);
            if (tokens >= 1d) {
                tokens -= 1d;
                return Decision.PERMITTED;
            }
            double nanosUntilNextToken = (1d - tokens) / refillPerNano;
            long retryAfter = Math.max(1L, (long) Math.ceil(nanosUntilNextToken / NANOS_PER_SECOND - 1e-9));
            return new Decision(false, retryAfter);
        }

        synchronized boolean isIdle(long nowNanos) {
            return nowNanos - lastNanos >= windowNanos;
        }

        private void refill(long nowNanos) {
            long elapsed = nowNanos - lastNanos;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
                lastNanos = nowNanos;
            }
        }
    }
}
