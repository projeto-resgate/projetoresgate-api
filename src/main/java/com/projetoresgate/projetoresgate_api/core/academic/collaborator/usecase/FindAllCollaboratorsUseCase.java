package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import org.springframework.data.domain.Page;

public interface FindAllCollaboratorsUseCase {
    Page<CollaboratorSummaryResponse> handle(FindAllCollaboratorsQuery query);
}
