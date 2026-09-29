package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindFamilyGroupNaturalPersonsQuery(UUID familyGroupId, Pageable pageable) {
}
