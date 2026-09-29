package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command;

import java.util.UUID;

public record AddNaturalPersonToFamilyGroupCommand(
        UUID familyGroupId,
        UUID naturalPersonId
) {
    public AddNaturalPersonToFamilyGroupCommand withFamilyGroupId(UUID familyGroupId) {
        return new AddNaturalPersonToFamilyGroupCommand(familyGroupId, naturalPersonId);
    }
}
