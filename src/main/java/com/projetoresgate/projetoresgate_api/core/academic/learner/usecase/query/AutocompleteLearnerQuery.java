package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query;

public record AutocompleteLearnerQuery(
        String searchTerm,
        int limit
) {
}
