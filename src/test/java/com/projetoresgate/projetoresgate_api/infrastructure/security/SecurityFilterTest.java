package com.projetoresgate.projetoresgate_api.infrastructure.security;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityFilter - Test")
class SecurityFilterTest {

    @Mock
    private ITokenService tokenService;

    @Mock
    private UserRepository userRepository;

    private SecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter(tokenService, userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar quando o token é válido e a versão coincide")
    void doFilter_ShouldAuthenticateWhenTokenVersionMatches() throws ServletException, IOException {
        User user = User.create("test@example.com", "encoded", "Test User", "tester");
        String token = "valid-token";
        String subject = user.getId().toString();

        when(tokenService.validateToken(token)).thenReturn(subject);
        when(userRepository.findByIdWithRoles(UUID.fromString(subject))).thenReturn(Optional.of(user));
        when(tokenService.getTokenVersion(token)).thenReturn(user.getTokenVersion());

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
    }

    @Test
    @DisplayName("Não deve autenticar quando o token está com a versão defasada")
    void doFilter_ShouldRejectTokenWithStaleVersion() throws ServletException, IOException {
        User user = User.create("test@example.com", "encoded", "Test User", "tester");
        String token = "stale-token";
        String subject = user.getId().toString();

        when(tokenService.validateToken(token)).thenReturn(subject);
        when(userRepository.findByIdWithRoles(UUID.fromString(subject))).thenReturn(Optional.of(user));
        when(tokenService.getTokenVersion(token)).thenReturn(user.getTokenVersion() + 1);

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Não deve autenticar quando o usuário não for encontrado")
    void doFilter_ShouldRejectWhenUserNotFound() throws ServletException, IOException {
        String token = "valid-token";
        String subject = UUID.randomUUID().toString();

        when(tokenService.validateToken(token)).thenReturn(subject);
        when(userRepository.findByIdWithRoles(UUID.fromString(subject))).thenReturn(Optional.empty());

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Não deve autenticar quando o token é inválido")
    void doFilter_ShouldRejectInvalidToken() throws ServletException, IOException {
        String token = "invalid-token";

        when(tokenService.validateToken(token)).thenThrow(new RuntimeException("Token inválido"));

        filter.doFilter(requestWith(token), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private MockHttpServletRequest requestWith(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
