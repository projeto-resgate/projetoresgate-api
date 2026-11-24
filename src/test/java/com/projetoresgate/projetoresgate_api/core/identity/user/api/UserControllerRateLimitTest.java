package com.projetoresgate.projetoresgate_api.core.identity.user.api;

import tools.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.*;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ICookieService;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, properties = {
        "api.rate-limit.max-requests=2",
        "api.rate-limit.window-seconds=60",
        "cors.allowed-origins=https://app.exemplo.org"
})
@Import(SecurityConfigurations.class)
@DisplayName("UserController - limite de requisições (integração)")
class UserControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;
    @MockitoBean
    private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private ITokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean
    private LogoutUseCase logoutUseCase;
    @MockitoBean
    private LogoutAllUseCase logoutAllUseCase;
    @MockitoBean
    private CreateUserUseCase createUserUseCase;
    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;
    @MockitoBean
    private SoftDeleteUserUseCase softDeleteUserUseCase;
    @MockitoBean
    private FindUserByIdUseCase findUserByIdUseCase;
    @MockitoBean
    private ICookieService cookieService;

    private void login() throws Exception {
        mockMvc.perform(post("/user/login")
                .with(csrf())
                .header("Origin", "https://app.exemplo.org")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
    }

    @Test
    @DisplayName("A 3ª tentativa de login deve voltar 429, com Retry-After, JSON e cabeçalho CORS (o front consegue ler)")
    void login_shouldReturn429WithCorsHeaders() throws Exception {
        login();
        login();

        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .header("Origin", "https://app.exemplo.org")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Access-Control-Allow-Origin", "https://app.exemplo.org"))
                .andExpect(jsonPath("$.code").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value(containsString("Tente novamente em")));
    }

    @Test
    @DisplayName("Requisições de preflight CORS não devem gastar o limite")
    void preflight_shouldNotConsumeLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(options("/user/login")
                            .header("Origin", "https://app.exemplo.org")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk());
        }
    }
}
