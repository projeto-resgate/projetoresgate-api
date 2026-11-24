package com.projetoresgate.projetoresgate_api.core.identity.user.api;

import tools.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.CreateUserCommand;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, properties = "springdoc.swagger-ui.enabled=false")
@Import(SecurityConfigurations.class)
@DisplayName("UserController - cadastro público desligado (produção)")
class UserControllerRegistrationBlockedTest {

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

    @Test
    @DisplayName("POST /user - Sem login deve retornar 401 e não criar usuário")
    void createUser_shouldReturn401_whenAnonymous() throws Exception {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "password123");

        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());

        verify(createUserUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /user - Autenticado continua podendo criar usuário")
    void createUser_shouldReturn201_whenAuthenticated() throws Exception {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "password123");
        when(createUserUseCase.handle(any(CreateUserCommand.class)))
                .thenReturn(User.create("john@test.com", "encoded", "John Doe", "johny"));

        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /user/login - Continua público")
    void login_shouldRemainPublic() throws Exception {
        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Swagger desligado (produção): a documentação da API exige login")
    void swagger_shouldRequireAuthentication_whenDisabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Saúde da aplicação é pública (healthcheck); métricas e info continuam exigindo login")
    void actuator_healthIsPublic_butMetricsAreNot() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isNotFound());

        mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/info")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    }
}
