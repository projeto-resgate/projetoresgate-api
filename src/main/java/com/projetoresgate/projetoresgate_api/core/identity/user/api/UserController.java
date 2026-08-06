package com.projetoresgate.projetoresgate_api.core.identity.user.api;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.RefreshTokenResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.UserResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.FindUserByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.RefreshTokenQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.UserDetailsImpl;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ICookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/user")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final LogoutAllUseCase logoutAllUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final SoftDeleteUserUseCase softDeleteUserUseCase;
    private final FindUserUseCase findUserUseCase;
    private final ICookieService cookieService;

    @Autowired
    public UserController(AuthenticateUserUseCase authenticateUserUseCase,
                          RequestPasswordResetUseCase requestPasswordResetUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          LogoutAllUseCase logoutAllUseCase,
                          CreateUserUseCase createUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          SoftDeleteUserUseCase softDeleteUserUseCase,
                          FindUserUseCase findUserUseCase,
                          ICookieService cookieService) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.softDeleteUserUseCase = softDeleteUserUseCase;
        this.findUserUseCase = findUserUseCase;
        this.cookieService = cookieService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Realiza o login do usuário e retorna um token JWT + refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody @Valid AuthenticateUserQuery query,
            HttpServletResponse response) {
        AuthenticationResponse authResponse = authenticateUserUseCase.handle(query);

        cookieService.setRefreshTokenCookie(response, authResponse.refreshToken());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar redefinição de senha", description = "Envia um e-mail com o link para redefinição de senha.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de redefinição enviado (se o usuário existir)")
    })
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordCommand command) {
        requestPasswordResetUseCase.handle(command.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha do usuário usando o token recebido por e-mail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordCommand command) {
        resetPasswordUseCase.handle(command.token(), command.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acesso", description = "Gera um novo access token usando o refresh token do cookie.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token renovado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido ou expirado")
    })
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InternalException("Cookie de renovação não fornecido");
        }

        RefreshTokenQuery query = new RefreshTokenQuery(refreshToken);
        RefreshTokenResponse refreshResponse = refreshTokenUseCase.handle(query);

        cookieService.setRefreshTokenCookie(httpResponse, refreshResponse.refreshToken());

        return ResponseEntity.ok(refreshResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Fazer logout", description = "Revoga o refresh token do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso")
    })
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse httpResponse) {

        if (nonNull(refreshToken) && !refreshToken.isBlank()) {
            LogoutCommand command = new LogoutCommand(refreshToken);
            logoutUseCase.handle(command);
        }

        cookieService.removeRefreshTokenCookie(httpResponse);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Fazer logout de todos os dispositivos", description = "Revoga todos os refresh tokens do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout de todos os dispositivos realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletResponse httpResponse) {

        LogoutAllCommand command = new LogoutAllCommand(userDetails.getUser().getId());
        logoutAllUseCase.handle(command);

        cookieService.removeRefreshTokenCookie(httpResponse);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponse> findUser(@PathVariable UUID id) {
        User user = findUserUseCase.handle(new FindUserByIdQuery(id));
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já existente")
    })
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateUserCommand cmd) {
        UUID userId = createUserUseCase.handle(cmd).getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(userId).toUri();
        return ResponseEntity.created(uri).body(userId);
    }

    @PutMapping
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<Void> update(@RequestBody UpdateUserCommand cmd,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        updateUserUseCase.handle(cmd.withId(userDetails.getUser().getId()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Deletar usuário", description = "Realiza a exclusão lógica do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    })
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        softDeleteUserUseCase.handle(new SoftDeleteUserCommand(userDetails.getUser().getId()));
        return ResponseEntity.noContent().build();
    }
}
