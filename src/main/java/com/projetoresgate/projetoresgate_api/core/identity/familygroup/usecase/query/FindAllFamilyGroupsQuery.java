package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query;

import org.springframework.data.domain.Pageable;

public record FindAllFamilyGroupsQuery(
        String name,
        Pageable pageable
) {
}
