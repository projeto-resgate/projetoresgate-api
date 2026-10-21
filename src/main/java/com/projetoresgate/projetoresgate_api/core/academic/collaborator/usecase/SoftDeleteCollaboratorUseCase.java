package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.SoftDeleteCollaboratorCommand;

public interface SoftDeleteCollaboratorUseCase {
    void handle(SoftDeleteCollaboratorCommand command);
}
