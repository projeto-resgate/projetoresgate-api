package com.projetoresgate.projetoresgate_api.core.user.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailRequest(@NotBlank String token) {
}
