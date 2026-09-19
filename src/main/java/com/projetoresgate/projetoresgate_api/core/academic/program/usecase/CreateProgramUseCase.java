package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.CreateProgramCommand;

public interface CreateProgramUseCase {
    ProgramResponse handle(CreateProgramCommand command);
}