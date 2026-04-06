package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordCommand(
        @NotBlank @Email String email
) {
}
