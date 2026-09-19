package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.SoftDeleteProgramCommand;

public interface SoftDeleteProgramUseCase {
    void handle(SoftDeleteProgramCommand command);
}