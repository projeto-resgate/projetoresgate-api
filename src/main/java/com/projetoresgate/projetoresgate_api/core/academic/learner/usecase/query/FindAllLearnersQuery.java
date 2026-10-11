package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindAllLearnersQuery(
        String searchTerm,
        LearnerStatus status,
        UUID institutionId,
        UUID schoolGradeId,
        Integer academicYear,
        Pageable pageable
) {
}
