package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;

import java.util.UUID;

public record FamilyGroupSummaryResponse(
        UUID id,
        String friendlyId,
        String name,
        Long registeredPeopleCount
) {
    public static FamilyGroupSummaryResponse fromEntity(FamilyGroup entity, Long registeredPeopleCount) {
        return new FamilyGroupSummaryResponse(
                entity.getId(),
                entity.getFriendlyId(),
                entity.getName(),
                registeredPeopleCount
        );
    }
}
