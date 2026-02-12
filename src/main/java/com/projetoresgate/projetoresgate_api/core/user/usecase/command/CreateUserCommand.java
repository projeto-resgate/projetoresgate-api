package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserCommand(
        @NotBlank(message = "O nome não pode ser vazio.")
        String name,

        @NotBlank(message = "O e-mail não pode ser vazio.")
        @Email(message = "O formato do e-mail é inválido.")
        String email,

        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String password
) {
}
