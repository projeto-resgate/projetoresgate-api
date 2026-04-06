package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordCommand(
        @NotBlank String token,
        @NotBlank @Size(min = 6) String newPassword
) {
}
