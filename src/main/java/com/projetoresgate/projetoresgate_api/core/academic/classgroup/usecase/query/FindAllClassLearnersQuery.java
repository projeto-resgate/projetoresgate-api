package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindAllClassLearnersQuery(
        UUID classGroupId,
        String searchTerm,
        Boolean active,
        Pageable pageable
) {
}
