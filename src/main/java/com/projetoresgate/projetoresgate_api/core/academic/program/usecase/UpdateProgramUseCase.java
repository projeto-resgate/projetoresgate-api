package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.UpdateProgramCommand;

public interface UpdateProgramUseCase {
    ProgramResponse handle(UpdateProgramCommand command);
}