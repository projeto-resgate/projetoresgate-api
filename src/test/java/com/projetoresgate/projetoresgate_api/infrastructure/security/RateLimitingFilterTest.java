package com.projetoresgate.projetoresgate_api.infrastructure.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter;
import com.projetoresgate.projetoresgate_api.infrastructure.ratelimit.TokenBucketRateLimiter.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RateLimitingFilter - Test")
class RateLimitingFilterTest {

    private static final Duration MINUTE = Duration.ofSeconds(60);
    private final JsonMapper objectMapper = new JsonMapper();

    private RateLimitingFilter filter(boolean enabled, int auth, int pub, int general, int trustedProxies) {
        return new RateLimitingFilter(enabled, new Policy(auth, MINUTE), new Policy(pub, MINUTE),
                new Policy(general, MINUTE), trustedProxies, new TokenBucketRateLimiter(new AtomicLong()::get));
    }

    private MockHttpServletResponse call(RateLimitingFilter filter, String method, String uri, String ip,
                                         String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        if (forwardedFor != null) request.addHeader("X-Forwarded-For", forwardedFor);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private MockHttpServletResponse call(RateLimitingFilter filter, String method, String uri, String ip)
            throws Exception {
        return call(filter, method, uri, ip, null);
    }

    @Test
    @DisplayName("Login: deve bloquear a partir da 6ª tentativa com 429, Retry-After e JSON válido")
    void login_shouldReturn429WithValidJson() throws Exception {
        RateLimitingFilter filter = filter(true, 5, 20, 300, 0);

        for (int i = 0; i < 5; i++) {
            assertEquals(200, call(filter, "POST", "/user/login", "10.0.0.1").getStatus(), "tentativa " + (i + 1));
        }
        MockHttpServletResponse blocked = call(filter, "POST", "/user/login", "10.0.0.1");

        assertEquals(429, blocked.getStatus());
        assertEquals("12", blocked.getHeader("Retry-After"));
        assertTrue(blocked.getContentType().startsWith("application/json"));
        JsonNode body = objectMapper.readTree(blocked.getContentAsString());
        assertEquals("Too Many Requests", body.get("code").asText());
        assertTrue(body.get("message").asText().contains("12 segundos"));
    }

    @Test
    @DisplayName("Forgot/reset de senha devem usar o mesmo limite estrito do login")
    void passwordRoutes_shouldBeInAuthTier() throws Exception {
        RateLimitingFilter filter = filter(true, 2, 20, 300, 0);

        assertEquals(200, call(filter, "POST", "/user/forgot-password", "10.0.0.1").getStatus());
        assertEquals(200, call(filter, "POST", "/user/reset-password", "10.0.0.1").getStatus());
        assertEquals(429, call(filter, "POST", "/user/login", "10.0.0.1").getStatus(), "o balde AUTH é um só");
    }

    @Test
    @DisplayName("Rotas públicas de escrita devem ter limite próprio, separado do login")
    void publicRoutes_shouldHaveOwnTier() throws Exception {
        RateLimitingFilter filter = filter(true, 1, 2, 300, 0);

        assertEquals(200, call(filter, "POST", "/natural-person", "10.0.0.1").getStatus());
        assertEquals(200, call(filter, "POST", "/natural-person/request-email-confirmation", "10.0.0.1").getStatus());
        assertEquals(429, call(filter, "POST", "/natural-person/confirm-email/abc", "10.0.0.1").getStatus());
        assertEquals(200, call(filter, "POST", "/user/login", "10.0.0.1").getStatus(), "login tem balde próprio");
    }

    @Test
    @DisplayName("Rotas comuns devem ter o limite geral, e GET /natural-person não é a rota pública POST")
    void generalTier_shouldApplyToEverythingElse() throws Exception {
        RateLimitingFilter filter = filter(true, 5, 1, 3, 0);

        for (int i = 0; i < 3; i++) {
            assertEquals(200, call(filter, "GET", "/natural-person", "10.0.0.1").getStatus());
        }
        assertEquals(429, call(filter, "GET", "/learner", "10.0.0.1").getStatus());
        assertEquals(200, call(filter, "POST", "/natural-person", "10.0.0.1").getStatus(), "POST usa a camada pública");
    }

    @Test
    @DisplayName("IPs diferentes devem ter limites independentes")
    void differentIps_shouldBeIndependent() throws Exception {
        RateLimitingFilter filter = filter(true, 1, 20, 300, 0);

        assertEquals(200, call(filter, "POST", "/user/login", "10.0.0.1").getStatus());
        assertEquals(429, call(filter, "POST", "/user/login", "10.0.0.1").getStatus());
        assertEquals(200, call(filter, "POST", "/user/login", "10.0.0.2").getStatus());
    }

    @Test
    @DisplayName("Preflight CORS (OPTIONS) não deve gastar ficha")
    void options_shouldBypass() throws Exception {
        RateLimitingFilter filter = filter(true, 1, 20, 1, 0);

        for (int i = 0; i < 10; i++) {
            assertEquals(200, call(filter, "OPTIONS", "/user/login", "10.0.0.1").getStatus());
        }
        assertEquals(200, call(filter, "POST", "/user/login", "10.0.0.1").getStatus());
    }

    @Test
    @DisplayName("Desligado por configuração, não deve limitar nada")
    void disabled_shouldNotLimit() throws Exception {
        RateLimitingFilter filter = filter(false, 1, 1, 1, 0);

        for (int i = 0; i < 10; i++) {
            assertEquals(200, call(filter, "POST", "/user/login", "10.0.0.1").getStatus());
        }
    }

    @Test
    @DisplayName("Sem proxy confiável, X-Forwarded-For forjado deve ser ignorado")
    void forgedForwardedFor_shouldBeIgnoredByDefault() throws Exception {
        RateLimitingFilter filter = filter(true, 2, 20, 300, 0);

        for (int i = 0; i < 2; i++) {
            call(filter, "POST", "/user/login", "10.0.0.1", "9.9.9." + i);
        }
        MockHttpServletResponse blocked = call(filter, "POST", "/user/login", "10.0.0.1", "9.9.9.200");

        assertEquals(429, blocked.getStatus(), "trocar o cabeçalho não deve burlar o limite");
    }

    @Test
    @DisplayName("Com 1 proxy confiável, deve usar o IP que o proxy acrescentou (à direita), não o forjado")
    void trustedProxy_shouldUseRightmostEntry() throws Exception {
        RateLimitingFilter filter = filter(true, 1, 20, 300, 1);

        assertEquals(200, call(filter, "POST", "/user/login", "172.18.0.2", "6.6.6.6, 203.0.113.5").getStatus());
        assertEquals(429, call(filter, "POST", "/user/login", "172.18.0.2", "7.7.7.7, 203.0.113.5").getStatus(),
                "mesmo cliente real, apesar de forjar outra entrada");
        assertEquals(200, call(filter, "POST", "/user/login", "172.18.0.2", "6.6.6.6, 203.0.113.99").getStatus(),
                "outro cliente real");
    }

    @Test
    @DisplayName("Com proxy confiável mas sem cabeçalho (ou curto demais), deve usar o IP da conexão")
    void trustedProxy_shouldFallBackToRemoteAddr() throws Exception {
        RateLimitingFilter filter = filter(true, 1, 20, 300, 2);

        assertEquals(200, call(filter, "POST", "/user/login", "172.18.0.2").getStatus());
        assertEquals(429, call(filter, "POST", "/user/login", "172.18.0.2", "203.0.113.5").getStatus(),
                "cabeçalho com 1 entrada, esperado 2: cai para o remoteAddr");
    }
}
