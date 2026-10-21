package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import java.util.UUID;

public record SoftDeleteClassCollaboratorCommand(
        UUID classGroupId,
        UUID id
) {
}
