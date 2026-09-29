package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.RemoveNaturalPersonFromFamilyGroupCommand;

public interface RemoveNaturalPersonFromFamilyGroupUseCase {
    void handle(RemoveNaturalPersonFromFamilyGroupCommand command);
}
