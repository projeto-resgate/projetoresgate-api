package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ClassGroupResponse(
        UUID id,
        String title,
        UUID programId,
        String programName,
        UUID institutionId,
        String institutionName,
        List<SchoolGradeNameResponse> schoolGrades,
        Integer studentProjections,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer durationInMonths,
        String note,
        boolean active,
        LocalDateTime dateCreated
) {
    public static ClassGroupResponse fromEntity(ClassGroup entity, String programName, String institutionName) {
        return new ClassGroupResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getProgramId(),
                programName,
                entity.getInstitutionId(),
                institutionName,
                entity.getSchoolGradeList().stream()
                        .sorted(Comparator.comparing(schoolGrade -> schoolGrade.getGradeOrder()))
                        .map(schoolGrade -> new SchoolGradeNameResponse(schoolGrade.getId(), schoolGrade.getName(),
                                schoolGrade.getAbbreviation(), schoolGrade.getGradeOrder()))
                        .toList(),
                entity.getStudentProjections(),
                entity.getStartDate(),
                entity.getEndDate(),
                resolveDurationInMonths(entity),
                entity.getNote(),
                entity.isActiveOn(LocalDateTime.now()),
                entity.getDateCreated()
        );
    }

    private static Integer resolveDurationInMonths(ClassGroup entity) {
        if (entity.getStartDate() == null || entity.getEndDate() == null) {
            return null;
        }
        return (int) ChronoUnit.MONTHS.between(entity.getStartDate(), entity.getEndDate());
    }
}
