package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query;

import org.springframework.data.domain.Pageable;

public record FindAllCollaboratorsQuery(
        String searchTerm,
        Pageable pageable
) {
}
