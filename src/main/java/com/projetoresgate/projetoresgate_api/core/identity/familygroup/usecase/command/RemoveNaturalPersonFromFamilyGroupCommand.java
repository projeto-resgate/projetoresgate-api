package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command;

import java.util.UUID;

public record RemoveNaturalPersonFromFamilyGroupCommand(
        UUID familyGroupId,
        UUID naturalPersonId
) {
    public RemoveNaturalPersonFromFamilyGroupCommand withFamilyGroupId(UUID familyGroupId) {
        return new RemoveNaturalPersonFromFamilyGroupCommand(familyGroupId, naturalPersonId);
    }
}
