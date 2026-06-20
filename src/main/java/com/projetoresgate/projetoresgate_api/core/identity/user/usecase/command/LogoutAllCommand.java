package com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command;

import java.util.UUID;

public record LogoutAllCommand(
        UUID userId
) {
}

