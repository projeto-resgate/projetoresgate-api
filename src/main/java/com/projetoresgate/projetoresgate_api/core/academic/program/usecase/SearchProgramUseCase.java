package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.SearchProgramQuery;
import org.springframework.data.domain.Page;

public interface SearchProgramUseCase {
    Page<ProgramResponse> handle(SearchProgramQuery query);
}