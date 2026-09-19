package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.AutocompleteLegalPersonQuery;

import java.util.List;

public interface AutocompleteLegalPersonUseCase {

    List<LegalPersonSummaryResponse> handle(AutocompleteLegalPersonQuery query);
}