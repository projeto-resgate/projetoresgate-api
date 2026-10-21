package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindCollaboratorByIdQuery;

public interface FindCollaboratorByIdUseCase {
    CollaboratorResponse handle(FindCollaboratorByIdQuery query);
}
