package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.AutocompleteNaturalPersonQuery;

import java.util.List;

public interface AutocompleteNaturalPersonUseCase {

    List<NaturalPersonSummaryResponse> handle(AutocompleteNaturalPersonQuery query);
}