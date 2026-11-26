package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRow;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

public record ClassLearnerResponse(
        UUID id,
        UUID classGroupId,
        UUID learnerId,
        String learnerName,
        String learnerCpf,
        String learnerRg,
        LearnerStatus learnerStatus,
        boolean active,
        LocalDateTime dateCreated
) {
    public static ClassLearnerResponse fromEntity(ClassLearner entity, String learnerName, String learnerCpf,
                                                  String learnerRg, LearnerStatus learnerStatus) {
        return new ClassLearnerResponse(
                entity.getId(),
                entity.getClassGroup().getId(),
                entity.getLearnerId(),
                learnerName,
                learnerCpf,
                learnerRg,
                learnerStatus,
                entity.isActive(),
                entity.getDateCreated()
        );
    }

    public static ClassLearnerResponse fromRow(ClassLearnerRow row) {
        return new ClassLearnerResponse(
                row.getId(),
                row.getClassGroupId(),
                row.getLearnerId(),
                row.getLearnerName(),
                row.getLearnerCpf(),
                row.getLearnerRg(),
                isNull(row.getLearnerStatus()) ? null : LearnerStatus.valueOf(row.getLearnerStatus()),
                row.getActive(),
                row.getDateCreated()
        );
    }
}
