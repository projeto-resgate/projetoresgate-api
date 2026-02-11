package com.projetoresgate.projetoresgate_api.modules.user.usecase.command;

import java.util.UUID;

public record SoftDeleteUserCommand(UUID id) {
}
