package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;

import java.util.List;
import java.util.Set;

public interface FindLegalPersonSummariesUseCase {

    List<LegalPersonSummaryResponse> handle(Set<java.util.UUID> ids);
}