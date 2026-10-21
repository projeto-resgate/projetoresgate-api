package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command;

import java.util.UUID;

public record SoftDeleteCollaboratorCommand(
        UUID id
) {
}
