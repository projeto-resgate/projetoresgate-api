package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramByIdQuery;

public interface FindProgramByIdUseCase {
    ProgramResponse handle(FindProgramByIdQuery query);
}