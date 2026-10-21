package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassCollaboratorCommand;

public interface UpdateClassCollaboratorUseCase {
    ClassCollaboratorResponse handle(UpdateClassCollaboratorCommand command);
}
