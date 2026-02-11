package com.projetoresgate.projetoresgate_api.core.user.api.dto;

import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;

import java.util.Set;

public record AuthenticationResponse(
        String accessToken,
        String userId,
        String name,
        String email,
        Set<UserRole> roles,
        boolean isEmailVerified
) {
}
