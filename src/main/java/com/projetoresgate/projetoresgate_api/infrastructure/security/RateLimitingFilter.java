package com.projetoresgate.projetoresgate_api.infrastructure.security;

import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter;
import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter.Decision;
import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter.Policy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    enum Tier {
        AUTH("auth"), PUBLIC("public"), GENERAL("general");

        final String prefix;

        Tier(String prefix) {
            this.prefix = prefix;
        }
    }

    private record Rule(HttpMethod method, String pattern, Tier tier) {
    }

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<Rule> RULES = List.of(
            new Rule(HttpMethod.POST, "/user/login", Tier.AUTH),
            new Rule(HttpMethod.POST, "/user/refresh", Tier.AUTH),
            new Rule(HttpMethod.POST, "/user/forgot-password", Tier.AUTH),
            new Rule(HttpMethod.POST, "/user/reset-password", Tier.AUTH),
            new Rule(HttpMethod.POST, "/user", Tier.PUBLIC),
            new Rule(HttpMethod.POST, "/natural-person", Tier.PUBLIC),
            new Rule(HttpMethod.POST, "/natural-person/request-email-confirmation", Tier.PUBLIC),
            new Rule(HttpMethod.POST, "/natural-person/confirm-email/**", Tier.PUBLIC)
    );

    private final boolean enabled;
    private final Policy authPolicy;
    private final Policy publicPolicy;
    private final Policy generalPolicy;
    private final int trustedProxies;
    private final TokenBucketRateLimiter limiter;

    @Autowired
    public RateLimitingFilter(
            @Value("${api.rate-limit.enabled:true}") boolean enabled,
            @Value("${api.rate-limit.max-requests:5}") int authMaxRequests,
            @Value("${api.rate-limit.window-seconds:60}") int authWindowSeconds,
            @Value("${api.rate-limit.public.max-requests:20}") int publicMaxRequests,
            @Value("${api.rate-limit.public.window-seconds:60}") int publicWindowSeconds,
            @Value("${api.rate-limit.general.max-requests:300}") int generalMaxRequests,
            @Value("${api.rate-limit.general.window-seconds:60}") int generalWindowSeconds,
            @Value("${api.rate-limit.trusted-proxies:0}") int trustedProxies) {
        this(enabled,
                new Policy(authMaxRequests, Duration.ofSeconds(authWindowSeconds)),
                new Policy(publicMaxRequests, Duration.ofSeconds(publicWindowSeconds)),
                new Policy(generalMaxRequests, Duration.ofSeconds(generalWindowSeconds)),
                trustedProxies,
                new TokenBucketRateLimiter());
    }

    RateLimitingFilter(boolean enabled, Policy authPolicy, Policy publicPolicy, Policy generalPolicy,
                       int trustedProxies, TokenBucketRateLimiter limiter) {
        this.enabled = enabled;
        this.authPolicy = authPolicy;
        this.publicPolicy = publicPolicy;
        this.generalPolicy = generalPolicy;
        this.trustedProxies = Math.max(0, trustedProxies);
        this.limiter = limiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Tier tier = resolveTier(request);
        String ip = resolveClientIp(request);

        Decision decision = limiter.tryAcquire(tier.prefix + ":" + ip, policyOf(tier));
        if (decision.allowed()) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Rate limit excedido: tier={} ip={} {} {}", tier, ip, request.getMethod(), request.getRequestURI());
        reject(response, decision.retryAfterSeconds());
    }

    Tier resolveTier(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (Rule rule : RULES) {
            if (rule.method().matches(request.getMethod()) && MATCHER.match(rule.pattern(), path)) {
                return rule.tier();
            }
        }
        return Tier.GENERAL;
    }

    String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (trustedProxies == 0) {
            return remoteAddr;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(forwardedFor)) {
            return remoteAddr;
        }
        String[] hops = forwardedFor.split(",");
        int index = hops.length - trustedProxies;
        if (index < 0) {
            return remoteAddr;
        }
        String candidate = hops[index].trim();
        return candidate.isEmpty() ? remoteAddr : candidate;
    }

    private Policy policyOf(Tier tier) {
        return switch (tier) {
            case AUTH -> authPolicy;
            case PUBLIC -> publicPolicy;
            case GENERAL -> generalPolicy;
        };
    }

    private void reject(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":\"Too Many Requests\",\"message\":\"Muitas requisições. "
                + "Tente novamente em " + retryAfterSeconds + " segundos.\"}");
    }
}
