package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import org.springframework.data.domain.Page;

public interface FindAllLearnersUseCase {
    Page<LearnerSummaryResponse> handle(FindAllLearnersQuery query);
}
