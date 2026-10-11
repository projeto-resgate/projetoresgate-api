package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;

import java.util.UUID;

public record LearnerSummaryResponse(
        UUID id,
        String name,
        String institutionName,
        Integer academicYear,
        boolean active
) {
    public static LearnerSummaryResponse fromEntity(Learner entity, String name, String institutionName) {
        return new LearnerSummaryResponse(
                entity.getId(),
                name,
                institutionName,
                entity.getAcademicYear(),
                entity.getStatus() == LearnerStatus.ACTIVE
        );
    }
}
