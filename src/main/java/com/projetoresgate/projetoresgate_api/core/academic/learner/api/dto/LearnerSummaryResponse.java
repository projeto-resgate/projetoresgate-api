package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerParticipationStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;

import java.util.List;
import java.util.UUID;

public record LearnerSummaryResponse(
        UUID id,
        String name,
        // programas das turmas em que o educando tem vínculo ativo, sem repetição e em ordem alfabética
        List<String> programNames,
        String institutionName,
        Integer academicYear,
        LearnerParticipationStatus status
) {
    public static LearnerSummaryResponse fromEntity(Learner entity, String name, List<String> programNames,
                                                    String institutionName, boolean inProgress) {
        return new LearnerSummaryResponse(
                entity.getId(),
                name,
                programNames,
                institutionName,
                entity.getAcademicYear(),
                participationStatus(entity, inProgress)
        );
    }

    private static LearnerParticipationStatus participationStatus(Learner entity, boolean inProgress) {
        if (entity.getStatus() == LearnerStatus.DROPPED_OUT) {
            return LearnerParticipationStatus.DROPPED_OUT;
        }
        return inProgress ? LearnerParticipationStatus.ACTIVE : LearnerParticipationStatus.NO_CLASS;
    }
}
