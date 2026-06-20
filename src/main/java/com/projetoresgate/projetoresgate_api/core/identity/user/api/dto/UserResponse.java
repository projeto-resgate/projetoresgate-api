package com.projetoresgate.projetoresgate_api.core.identity.user.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        String nickname,
        Set<UserRole> roles
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getRoles()
        );
    }
}