package com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command;

public record LogoutCommand(
        String refreshToken
) {
}

