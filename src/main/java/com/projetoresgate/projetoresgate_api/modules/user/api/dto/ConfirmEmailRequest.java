package com.projetoresgate.projetoresgate_api.modules.user.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(@NotBlank String token) {
}
