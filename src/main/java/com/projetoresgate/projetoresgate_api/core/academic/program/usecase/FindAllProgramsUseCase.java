package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindAllProgramsQuery;
import org.springframework.data.domain.Page;

public interface FindAllProgramsUseCase {
    Page<ProgramResponse> handle(FindAllProgramsQuery query);
}