package com.projetoresgate.projetoresgate_api.core.identity.user.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record RefreshTokenResponse(
        String accessToken,

        @JsonIgnore
        String refreshToken,

        long expiresIn,
        String tokenType
) {
}