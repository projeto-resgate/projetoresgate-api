package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserCommand(

        @NotBlank(message = "{validation.user.email.not.blank}")
        @Email(message = "{validation.user.email.invalid}")
        String email,

        @NotBlank(message = "{validation.user.password.not.blank}")
        @Size(min = 6, message = "{validation.user.password.size}")
        String password,

        @NotBlank(message = "{validation.user.name.not.blank}")
        String name
) {
}
