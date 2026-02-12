package com.projetoresgate.projetoresgate_api.core.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetoresgate.projetoresgate_api.config.security.WithMockCustomUser;
import com.projetoresgate.projetoresgate_api.core.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.user.api.dto.ConfirmEmailRequest;
import com.projetoresgate.projetoresgate_api.core.user.api.dto.ForgotPasswordRequest;
import com.projetoresgate.projetoresgate_api.core.user.api.dto.ResetPasswordRequest;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.*;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.ConfirmEmailCommand;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.SoftDeleteUserCommand;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.UpdateUserCommand;
import com.projetoresgate.projetoresgate_api.core.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.core.user.usecase.query.FindUserByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.SecurityConfigurations;
import com.projetoresgate.projetoresgate_api.infrastructure.services.TokenService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private CreateUserUseCase createUserUseCase;
    @MockitoBean
    private UpdateUserUseCase updateUserUseCase;
    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;
    @MockitoBean
    private SoftDeleteUserUseCase softDeleteUserUseCase;
    @MockitoBean
    private FindUserUseCase findUserUseCase;
    @MockitoBean
    private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;
    @MockitoBean
    private RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;
    @MockitoBean
    private ConfirmEmailUseCase confirmEmailUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /user - Deve retornar 201 Created ao criar usuário com sucesso")
    void createUser_shouldReturn201Created() throws Exception {
        CreateUserCommand command = new CreateUserCommand("test@example.com", "password123", "Test User");
        UUID newUserId = UUID.randomUUID();
        when(createUserUseCase.handle(any(CreateUserCommand.class))).thenReturn(newUserId);

        mockMvc.perform(post("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/user/" + newUserId))
                .andExpect(jsonPath("$").value(newUserId.toString()));
    }

    @Test
    @DisplayName("POST /user/login - Deve retornar 200 OK com token em login bem-sucedido")
    void login_shouldReturn200OkWithToken() throws Exception {
        AuthenticateUserQuery query = new AuthenticateUserQuery("test@example.com", "password123");
        AuthenticationResponse authResponse = new AuthenticationResponse("mock.jwt.token", UUID.randomUUID().toString(), "Test User", "test@example.com", Set.of(UserRole.USER), true);
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
    @WithMockCustomUser
    @DisplayName("GET /user/{id} - Deve retornar 200 OK com dados do usuário quando encontrado")
    void findUser_shouldReturn200OkWithUserData() throws Exception {
        UUID userId = UUID.randomUUID();
        User foundUser = new User("test@example.com", "password", "Found User");
        foundUser.setId(userId);
        when(findUserUseCase.handle(any(FindUserByIdQuery.class))).thenReturn(foundUser);

        mockMvc.perform(get("/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Found User"));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("GET /user/{id} - Deve retornar 404 Not Found quando usuário não encontrado")
    void findUser_shouldReturn404NotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(findUserUseCase.handle(any(FindUserByIdQuery.class))).thenThrow(new ResourceNotFoundException("Usuário não encontrado."));

        mockMvc.perform(get("/user/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("PUT /user - Deve retornar 200 OK em atualização bem-sucedida")
    void updateUser_shouldReturn200Ok() throws Exception {
        UpdateUserCommand command = new UpdateUserCommand(null, "New Name", null, null);
        doNothing().when(updateUserUseCase).handle(any(UpdateUserCommand.class));

        mockMvc.perform(put("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /user - Deve retornar 401 Unauthorized se usuário não autenticado")
    void updateUser_shouldReturn401Unauthorized() throws Exception {
        UpdateUserCommand command = new UpdateUserCommand(null, "New Name", null, null);

        mockMvc.perform(put("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("DELETE /user - Deve retornar 204 No Content em exclusão bem-sucedida")
    void deleteUser_shouldReturn204NoContent() throws Exception {
        doNothing().when(softDeleteUserUseCase).handle(any(SoftDeleteUserCommand.class));

        mockMvc.perform(delete("/user")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /user - Deve retornar 401 Unauthorized se usuário não autenticado")
    void deleteUser_shouldReturn401Unauthorized() throws Exception {
        mockMvc.perform(delete("/user")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /user/forgot-password - Deve retornar 200 OK")
    void forgotPassword_shouldReturn200Ok() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        doNothing().when(requestPasswordResetUseCase).handle(anyString());

        mockMvc.perform(post("/user/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /user/reset-password - Deve retornar 200 OK em sucesso")
    void resetPassword_shouldReturn200Ok() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "new-password");
        doNothing().when(resetPasswordUseCase).handle(anyString(), anyString());

        mockMvc.perform(post("/user/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /user/reset-password - Deve retornar 400 Bad Request em falha")
    void resetPassword_shouldReturn400BadRequest() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "new-password");
        doThrow(new InternalException("Token inválido")).when(resetPasswordUseCase).handle(anyString(), anyString());

        mockMvc.perform(post("/user/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /user/request-email-confirmation - Deve retornar 200 OK")
    void requestEmailConfirmation_shouldReturn200Ok() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        doNothing().when(requestEmailConfirmationUseCase).handle(anyString());

        mockMvc.perform(post("/user/request-email-confirmation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("POST /user/confirm-email - Deve retornar 200 OK em sucesso")
    void confirmEmail_shouldReturn200Ok() throws Exception {
        ConfirmEmailRequest request = new ConfirmEmailRequest("123456");
        doNothing().when(confirmEmailUseCase).handle(any(ConfirmEmailCommand.class));

        mockMvc.perform(post("/user/confirm-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /user/confirm-email - Deve retornar 401 Unauthorized se não autenticado")
    void confirmEmail_shouldReturn401Unauthorized() throws Exception {
        ConfirmEmailRequest request = new ConfirmEmailRequest("123456");

        mockMvc.perform(post("/user/confirm-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
