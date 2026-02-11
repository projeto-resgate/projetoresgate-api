package com.projetoresgate.projetoresgate_api.modules.user.api.dto;

import com.projetoresgate.projetoresgate_api.modules.user.domain.enums.UserRole;
import java.util.Set;

public record AuthenticationResponse(
        String access_token,
        String userId,
        String name,
        String email,
        Set<UserRole> roles,
        boolean isRegisterCompleted
) {
}
