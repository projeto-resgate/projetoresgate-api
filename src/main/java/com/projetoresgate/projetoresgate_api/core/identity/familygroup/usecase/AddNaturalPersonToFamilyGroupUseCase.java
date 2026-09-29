package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNaturalPersonToFamilyGroupCommand;

public interface AddNaturalPersonToFamilyGroupUseCase {
    void handle(AddNaturalPersonToFamilyGroupCommand command);
}
