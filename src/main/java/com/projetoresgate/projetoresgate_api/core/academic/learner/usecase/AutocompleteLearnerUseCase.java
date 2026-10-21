package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.AutocompleteLearnerQuery;

import java.util.List;

public interface AutocompleteLearnerUseCase {
    List<LearnerAutocompleteResponse> handle(AutocompleteLearnerQuery query);
}
