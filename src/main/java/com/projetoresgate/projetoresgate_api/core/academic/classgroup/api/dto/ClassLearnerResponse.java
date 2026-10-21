package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassLearnerResponse(
        UUID id,
        UUID classGroupId,
        UUID learnerId,
        String learnerName,
        String learnerCpf,
        LearnerStatus learnerStatus,
        boolean active,
        LocalDateTime dateCreated
) {
    public static ClassLearnerResponse fromEntity(ClassLearner entity, String learnerName, String learnerCpf,
                                                  LearnerStatus learnerStatus) {
        return new ClassLearnerResponse(
                entity.getId(),
                entity.getClassGroup().getId(),
                entity.getLearnerId(),
                learnerName,
                learnerCpf,
                learnerStatus,
                entity.isActive(),
                entity.getDateCreated()
        );
    }
}
