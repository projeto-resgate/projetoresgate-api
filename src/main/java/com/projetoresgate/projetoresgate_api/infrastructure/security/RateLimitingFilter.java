package com.projetoresgate.projetoresgate_api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final List<String> RATE_LIMITED_PATHS = List.of("/user/login", "/user/refresh");

    private final int maxRequests;
    private final Duration window;

    private final ConcurrentHashMap<String, List<Instant>> requestLog = new ConcurrentHashMap<>();

    public RateLimitingFilter(
            @Value("${api.rate-limit.max-requests:5}") int maxRequests,
            @Value("${api.rate-limit.window-seconds:60}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean shouldRateLimit = RATE_LIMITED_PATHS.stream().anyMatch(path::endsWith);

        if (shouldRateLimit) {
            String clientId = resolveClientId(request);
            Instant now = Instant.now();

            List<Instant> timestamps = requestLog.compute(clientId, (key, existing) -> {
                List<Instant> valid = (existing == null ? List.<Instant>of() : existing)
                        .stream()
                        .filter(t -> Duration.between(t, now).compareTo(window) < 0)
                        .collect(Collectors.toList());
                valid.add(now);
                return valid;
            });

            if (timestamps.size() > maxRequests) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":\"429\",\"message\":\"Muitas requisições. Tente novamente em \""
                        + window.toSeconds() + " segundos.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientId(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
