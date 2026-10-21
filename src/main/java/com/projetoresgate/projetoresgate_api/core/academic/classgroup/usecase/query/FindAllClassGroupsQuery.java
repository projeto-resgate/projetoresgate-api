package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindAllClassGroupsQuery(
        String searchTerm,
        UUID programId,
        UUID institutionId,
        Boolean active,
        Pageable pageable
) {
}
