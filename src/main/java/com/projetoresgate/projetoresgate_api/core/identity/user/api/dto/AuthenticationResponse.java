package com.projetoresgate.projetoresgate_api.core.identity.user.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;

import java.util.Set;

public record AuthenticationResponse(
        String accessToken,

        @JsonIgnore
        String refreshToken,

        long expiresIn,
        String tokenType,
        String userId,
        String name,
        String email,
        Set<UserRole> roles
) {
}