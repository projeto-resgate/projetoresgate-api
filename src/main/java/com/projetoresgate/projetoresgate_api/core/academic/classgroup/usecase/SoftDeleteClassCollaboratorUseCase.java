package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassCollaboratorCommand;

public interface SoftDeleteClassCollaboratorUseCase {
    void handle(SoftDeleteClassCollaboratorCommand command);
}
