package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.CreateCollaboratorCommand;

public interface CreateCollaboratorUseCase {
    CollaboratorResponse handle(CreateCollaboratorCommand command);
}
