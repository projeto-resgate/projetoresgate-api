package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

public record FamilyGroupSummaryResponse(
        UUID id,
        String friendlyId,
        String name,
        Long registeredPeopleCount,
        List<String> registeredPeopleNames
) {
    public static FamilyGroupSummaryResponse fromEntity(FamilyGroup entity, Long registeredPeopleCount,
                                                        List<String> registeredPeopleNames) {
        return new FamilyGroupSummaryResponse(
                entity.getId(),
                entity.getFriendlyId(),
                entity.getName(),
                registeredPeopleCount,
                isNull(registeredPeopleNames) ? Collections.emptyList() : registeredPeopleNames
        );
    }
}
