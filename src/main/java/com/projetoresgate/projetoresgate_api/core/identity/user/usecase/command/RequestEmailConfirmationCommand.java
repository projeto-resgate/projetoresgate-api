package com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestEmailConfirmationCommand(
        @NotBlank @Email String email
) {
}
