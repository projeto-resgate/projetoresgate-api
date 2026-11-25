package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestEmailConfirmationCommand(
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "O formato do e-mail é inválido.") String email
) {
}
