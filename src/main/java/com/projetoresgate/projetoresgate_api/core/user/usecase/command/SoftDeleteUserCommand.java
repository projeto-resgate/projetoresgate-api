package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import java.util.UUID;

public record SoftDeleteUserCommand(UUID id) {
}
