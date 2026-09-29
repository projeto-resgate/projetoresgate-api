package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query;

import org.springframework.data.domain.Pageable;

public record SearchFamilyGroupQuery(
        String name,
        Pageable pageable
) {
}
