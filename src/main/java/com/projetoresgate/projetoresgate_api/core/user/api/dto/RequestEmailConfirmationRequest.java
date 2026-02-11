package com.projetoresgate.projetoresgate_api.core.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestEmailConfirmationRequest(
        @NotBlank @Email String email
) {
}
