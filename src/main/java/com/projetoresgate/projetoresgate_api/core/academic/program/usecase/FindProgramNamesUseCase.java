package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramNamesQuery;

import java.util.List;

public interface FindProgramNamesUseCase {
    List<ProgramNameResponse> handle(FindProgramNamesQuery query);
}