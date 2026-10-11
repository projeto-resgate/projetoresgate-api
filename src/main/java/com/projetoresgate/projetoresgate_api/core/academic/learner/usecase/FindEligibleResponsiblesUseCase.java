package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.EligibleResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindEligibleResponsiblesQuery;
import org.springframework.data.domain.Page;

public interface FindEligibleResponsiblesUseCase {
    Page<EligibleResponsibleResponse> handle(FindEligibleResponsiblesQuery query);
}
