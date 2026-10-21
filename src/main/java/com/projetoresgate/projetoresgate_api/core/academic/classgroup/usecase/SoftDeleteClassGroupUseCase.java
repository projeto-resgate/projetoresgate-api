package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassGroupCommand;

public interface SoftDeleteClassGroupUseCase {
    void handle(SoftDeleteClassGroupCommand command);
}
