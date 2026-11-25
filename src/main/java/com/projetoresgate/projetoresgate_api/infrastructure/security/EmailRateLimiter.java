package com.projetoresgate.projetoresgate_api.infrastructure.security;

import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter;
import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter.Policy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;

@Component
public class EmailRateLimiter {

    private final boolean enabled;
    private final Policy policy;
    private final TokenBucketRateLimiter limiter = new TokenBucketRateLimiter();

    public EmailRateLimiter(
            @Value("${api.rate-limit.enabled:true}") boolean enabled,
            @Value("${api.rate-limit.email.max-requests:3}") int maxRequests,
            @Value("${api.rate-limit.email.window-seconds:3600}") int windowSeconds) {
        this.enabled = enabled;
        this.policy = new Policy(maxRequests, Duration.ofSeconds(windowSeconds));
    }

    public boolean tryAcquire(String email) {
        if (!enabled || !StringUtils.hasText(email)) {
            return true;
        }
        String key = "email:" + email.trim().toLowerCase(Locale.ROOT);
        return limiter.tryAcquire(key, policy).allowed();
    }
}
