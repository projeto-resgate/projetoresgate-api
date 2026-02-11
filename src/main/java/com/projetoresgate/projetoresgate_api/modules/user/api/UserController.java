package com.projetoresgate.projetoresgate_api.modules.user.api;

import com.projetoresgate.projetoresgate_api.modules.user.api.dto.*;
import com.projetoresgate.projetoresgate_api.modules.user.domain.User;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.*;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.ConfirmEmailCommand;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.SoftDeleteUserCommand;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.UpdateUserCommand;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.query.FindUserByIdQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final SoftDeleteUserUseCase softDeleteUserUseCase;
    private final FindUserUseCase findUserUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;

    @Autowired
    public UserController(CreateUserUseCase createUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          SoftDeleteUserUseCase softDeleteUserUseCase,
                          FindUserUseCase findUserUseCase,
                          RequestPasswordResetUseCase requestPasswordResetUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          RequestEmailConfirmationUseCase requestEmailConfirmationUseCase,
                          ConfirmEmailUseCase confirmEmailUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.softDeleteUserUseCase = softDeleteUserUseCase;
        this.findUserUseCase = findUserUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
        this.confirmEmailUseCase = confirmEmailUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Realiza o login do usuário e retorna um token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticateUserQuery query) {
        AuthenticationResponse response = authenticateUserUseCase.handle(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar redefinição de senha", description = "Envia um e-mail com o link para redefinição de senha.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de redefinição enviado (se o usuário existir)")
    })
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        requestPasswordResetUseCase.handle(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha", description = "Redefine a senha do usuário usando o token recebido por e-mail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        resetPasswordUseCase.handle(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request-email-confirmation")
    @Operation(summary = "Solicitar confirmação de e-mail", description = "Reenvia o e-mail de confirmação de conta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail de confirmação enviado")
    })
    public ResponseEntity<Void> requestEmailConfirmation(@RequestBody @Valid ForgotPasswordRequest request) {
        requestEmailConfirmationUseCase.handle(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-email")
    @Operation(summary = "Confirmar e-mail", description = "Confirma o e-mail do usuário usando o token recebido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail confirmado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Void> confirmEmail(@RequestBody @Valid ConfirmEmailRequest request, @AuthenticationPrincipal User user) {
        confirmEmailUseCase.handle(new ConfirmEmailCommand(request.token(), user));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os detalhes de um usuário específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponse> findUser(@PathVariable UUID id) {
        User user = findUserUseCase.handle(new FindUserByIdQuery(id));
        return ResponseEntity.ok(new UserResponse(user));
    }

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já existente")
    })
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateUserCommand cmd) {
        UUID userId = createUserUseCase.handle(cmd);
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
    public ResponseEntity<Void> update(@RequestBody UpdateUserCommand cmd, @AuthenticationPrincipal User user) {
        updateUserUseCase.handle(cmd.withId(user.getId()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Deletar usuário", description = "Realiza a exclusão lógica do usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso")
    })
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal User user) {
        softDeleteUserUseCase.handle(new SoftDeleteUserCommand(user.getId()));
        return ResponseEntity.noContent().build();
    }
}
