package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.UpdateCollaboratorCommand;

public interface UpdateCollaboratorUseCase {
    CollaboratorResponse handle(UpdateCollaboratorCommand command);
}
