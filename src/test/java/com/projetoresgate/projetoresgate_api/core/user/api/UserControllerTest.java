package com.projetoresgate.projetoresgate_api.core.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.identity.user.api.UserController;
import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.RefreshTokenResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.FindUserByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.RefreshTokenQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ICookieService;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfigurations.class)
@DisplayName("UserController - Test")
class UserControllerTest {

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
    private FindUserUseCase findUserUseCase;

    @MockitoBean
    private ICookieService cookieService;

    @Test
    @DisplayName("POST /user/login - Deve retornar 200 OK com token em login bem-sucedido")
    void login_shouldReturn200OkWithToken() throws Exception {
        AuthenticateUserQuery query = new AuthenticateUserQuery("test@example.com", "password123");
        AuthenticationResponse authResponse = new AuthenticationResponse(
                "mock.jwt.token",
                "mock.refresh.token",
                3600L,
                "Bearer",
                UUID.randomUUID().toString(),
                "Test User",
                "test@example.com",
                Set.of(UserRole.USER)
        );

        when(authenticateUserUseCase.handle(any(AuthenticateUserQuery.class))).thenReturn(authResponse);

        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("POST /user/login - Deve retornar 400 Bad Request quando validação falha")
    void login_shouldReturn400_whenValidationFails() throws Exception {
        AuthenticateUserQuery query = new AuthenticateUserQuery("", "123");

        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("O e-mail não pode ser vazio.")))
                .andExpect(content().string(containsString("A senha deve ter no mínimo 6 caracteres.")));
    }

    @Test
    @DisplayName("POST /user/login - Deve retornar 401 Unauthorized em falha de login")
    void login_shouldReturn401UnauthorizedOnFailure() throws Exception {
        AuthenticateUserQuery query = new AuthenticateUserQuery("test@example.com", "wrongpassword");
        when(authenticateUserUseCase.handle(any(AuthenticateUserQuery.class))).thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /user - Deve retornar 401 Unauthorized se usuário não autenticado")
    void deleteUser_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(delete("/user")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /user - Deve retornar 201 Created com o ID e Location ao criar usuário")
    void createUser_shouldReturn201CreatedWithIdAndLocation() throws Exception {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "password123");
        User user = User.create("john@test.com", "encoded-password", "John Doe", "johny");

        when(createUserUseCase.handle(any(CreateUserCommand.class))).thenReturn(user);

        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value(user.getId().toString()))
                .andExpect(header().string("Location", containsString("/user/" + user.getId())));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /user/{id} - Deve retornar 200 OK com os dados do usuário")
    void findUser_shouldReturn200OkWithUser() throws Exception {
        User user = User.create("john@test.com", "encoded-password", "John Doe", "johny");

        when(findUserUseCase.handle(any(FindUserByIdQuery.class))).thenReturn(user);

        mockMvc.perform(get("/user/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.nickname").value("johny"));
    }

    @Test
    @DisplayName("POST /user - Deve retornar 400 Bad Request quando a senha estiver vazia")
    void createUser_shouldReturn400_whenPasswordIsBlank() throws Exception {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "");

        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A senha não pode ser vazia.")));

        verify(createUserUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("POST /user - Deve retornar 400 Bad Request quando a senha tiver menos de 6 caracteres")
    void createUser_shouldReturn400_whenPasswordTooShort() throws Exception {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "123");

        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("A senha deve ter no mínimo 6 caracteres.")));

        verify(createUserUseCase, never()).handle(any());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /user - Deve retornar 200 OK ao atualizar usuário autenticado")
    void updateUser_shouldReturn200Ok() throws Exception {
        UpdateUserCommand command = new UpdateUserCommand(null, "New Name", "newnick", null, null);

        mockMvc.perform(put("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());

        verify(updateUserUseCase).handle(any(UpdateUserCommand.class));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /user - Deve retornar 204 No Content ao deletar usuário autenticado")
    void deleteUser_shouldReturn204NoContent() throws Exception {
        mockMvc.perform(delete("/user")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(softDeleteUserUseCase).handle(any(SoftDeleteUserCommand.class));
    }

    @Test
    @DisplayName("POST /user/forgot-password - Deve retornar 200 OK")
    void forgotPassword_shouldReturn200Ok() throws Exception {
        ForgotPasswordCommand command = new ForgotPasswordCommand("test@example.com");
        doNothing().when(requestPasswordResetUseCase).handle(anyString());

        mockMvc.perform(post("/user/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /user/forgot-password - Deve retornar 400 Bad Request quando validação falha")
    void forgotPassword_shouldReturn400_whenValidationFails() throws Exception {
        ForgotPasswordCommand command = new ForgotPasswordCommand("invalid-email");

        mockMvc.perform(post("/user/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /user/reset-password - Deve retornar 200 OK em sucesso")
    void resetPassword_shouldReturn200Ok() throws Exception {
        ResetPasswordCommand command = new ResetPasswordCommand("valid-token", "new-password");
        doNothing().when(resetPasswordUseCase).handle(anyString(), anyString());

        mockMvc.perform(post("/user/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /user/reset-password - Deve retornar 400 Bad Request quando validação falha")
    void resetPassword_shouldReturn400_whenValidationFails() throws Exception {
        ResetPasswordCommand command = new ResetPasswordCommand("", "123");

        mockMvc.perform(post("/user/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /user/reset-password - Deve retornar 400 Bad Request em falha de serviço")
    void resetPassword_shouldReturn400BadRequest_onServiceFailure() throws Exception {
        ResetPasswordCommand command = new ResetPasswordCommand("invalid-token", "new-password");
        doThrow(new InternalException("Token inválido")).when(resetPasswordUseCase).handle(anyString(), anyString());

        mockMvc.perform(post("/user/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /user/refresh - Deve retornar 200 OK e gerar novos tokens")
    void refresh_shouldReturn200OkAndNewTokens() throws Exception {
        String oldRefreshToken = "old-refresh-token-value";
        Cookie requestCookie = new Cookie("refreshToken", oldRefreshToken);

        RefreshTokenResponse mockResponse = new RefreshTokenResponse(
                "new.jwt.access.token",
                "new.jwt.refresh.token",
                3600L,
                "Bearer"
        );

        when(refreshTokenUseCase.handle(new RefreshTokenQuery(oldRefreshToken))).thenReturn(mockResponse);

        mockMvc.perform(post("/user/refresh")
                        .cookie(requestCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.jwt.access.token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());

        verify(refreshTokenUseCase).handle(new RefreshTokenQuery(oldRefreshToken));
        verify(cookieService).setRefreshTokenCookie(any(HttpServletResponse.class), eq("new.jwt.refresh.token"));
    }

    @Test
    @DisplayName("POST /user/refresh - Deve retornar 400 se o cookie não for enviado")
    void refresh_shouldReturn400WhenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/user/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(refreshTokenUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("POST /user/logout - Deve retornar 200 OK, invalidar o token e remover o cookie")
    @WithMockCustomUser
    void logout_shouldReturn200OkAndRemoveCookie() throws Exception {
        Cookie requestCookie = new Cookie("refreshToken", "valid-refresh-token");

        doNothing().when(logoutUseCase).handle(any(LogoutCommand.class));

        mockMvc.perform(post("/user/logout")
                        .cookie(requestCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(logoutUseCase).handle(new LogoutCommand("valid-refresh-token"));
        verify(cookieService).removeRefreshTokenCookie(any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("POST /user/logout-all - Deve retornar 200 OK, invalidar todos os tokens e remover o cookie")
    @WithMockCustomUser
    void logoutAll_shouldReturn200OkAndRemoveCookie() throws Exception {
        Cookie requestCookie = new Cookie("refreshToken", "valid-refresh-token");

        doNothing().when(logoutAllUseCase).handle(any());

        mockMvc.perform(post("/user/logout-all")
                        .cookie(requestCookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(logoutAllUseCase).handle(any());
        verify(cookieService).removeRefreshTokenCookie(any(HttpServletResponse.class));
    }
}