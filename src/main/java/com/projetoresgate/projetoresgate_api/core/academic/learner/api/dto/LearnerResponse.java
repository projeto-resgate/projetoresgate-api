package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record LearnerResponse(
        UUID id,
        UUID naturalPersonId,
        UUID institutionId,
        UUID schoolGradeId,
        boolean interestArts,
        ScholarshipCondition scholarshipCondition,
        Integer academicYear,
        String motherName,
        String schoolEnrollment,
        LearnerStatus status,
        List<ResponsibleResponse> responsibleList,
        LocalDateTime dateCreated
) {
    public static LearnerResponse fromEntity(Learner entity, Map<UUID, String> naturalPersonNamesById) {
        return new LearnerResponse(
                entity.getId(),
                entity.getNaturalPersonId(),
                entity.getInstitutionId(),
                entity.getSchoolGradeId(),
                entity.isInterestArts(),
                entity.getScholarshipCondition(),
                entity.getAcademicYear(),
                entity.getMotherName(),
                entity.getSchoolEnrollment(),
                entity.getStatus(),
                entity.getResponsibleList().stream()
                        .map(responsible -> ResponsibleResponse.fromEntity(
                                responsible, naturalPersonNamesById.get(responsible.getNaturalPersonId())))
                        .toList(),
                entity.getDateCreated()
        );
    }
}