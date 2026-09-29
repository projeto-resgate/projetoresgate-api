package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.SoftDeleteFamilyGroupCommand;

public interface SoftDeleteFamilyGroupUseCase {
    void handle(SoftDeleteFamilyGroupCommand command);
}
