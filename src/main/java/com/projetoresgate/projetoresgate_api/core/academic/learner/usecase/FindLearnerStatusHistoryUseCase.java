package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerStatusHistoryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerStatusHistoryQuery;

import java.util.List;

public interface FindLearnerStatusHistoryUseCase {
    List<LearnerStatusHistoryResponse> handle(FindLearnerStatusHistoryQuery query);
}
