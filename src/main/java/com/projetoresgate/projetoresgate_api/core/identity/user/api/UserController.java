package com.projetoresgate.projetoresgate_api.core.identity.user.api;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.RefreshTokenResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.*;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.RefreshTokenQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.security.UserDetailsImpl;
import com.projetoresgate.projetoresgate_api.infrastructure.services.CookieService;
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

import static java.util.Objects.nonNull;

@RestController
@RequestMapping("/user")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final LogoutAllUseCase logoutAllUseCase;
    private final CookieService cookieService;

    @Autowired
    public UserController(AuthenticateUserUseCase authenticateUserUseCase,
                          RequestPasswordResetUseCase requestPasswordResetUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          RequestEmailConfirmationUseCase requestEmailConfirmationUseCase,
                          ConfirmEmailUseCase confirmEmailUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          LogoutAllUseCase logoutAllUseCase,
                          CookieService cookieService) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
        this.confirmEmailUseCase = confirmEmailUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.logoutAllUseCase = logoutAllUseCase;
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

    @PostMapping("/request-email-confirmation")
    @Operation(summary = "Solicitar confirmação de e-mail", description = "Reenvia o e-mail de confirmação de conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de confirmação enviado")
    })
    public ResponseEntity<Void> requestEmailConfirmation(@RequestBody @Valid ForgotPasswordCommand command) {
        requestEmailConfirmationUseCase.handle(command.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-email/{token}")
    @Operation(summary = "Confirmar e-mail", description = "Confirma o e-mail do usuário usando o token recebido por link.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail confirmado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Void> confirmEmail(@PathVariable String token) {
        confirmEmailUseCase.handle(new ConfirmEmailCommand(token));
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
}
