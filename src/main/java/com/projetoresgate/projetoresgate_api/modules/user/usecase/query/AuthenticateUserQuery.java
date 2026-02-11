package com.projetoresgate.projetoresgate_api.modules.user.usecase.query;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticateUserQuery(
        @NotBlank(message = "{validation.user.email.not.blank}")
        @Email(message = "{validation.user.email.invalid}")
        String email,

        @NotBlank(message = "{validation.user.password.not.blank}")
        @Size(min = 6, message = "{validation.user.password.size}")
        String password
) {
}
