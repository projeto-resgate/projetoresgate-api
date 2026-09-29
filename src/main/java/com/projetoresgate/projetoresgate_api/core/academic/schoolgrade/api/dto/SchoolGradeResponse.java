package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchoolGradeResponse(
        UUID id,
        String name,
        Integer gradeOrder,
        LocalDateTime dateCreated
) {
    public static SchoolGradeResponse fromEntity(SchoolGrade entity) {
        return new SchoolGradeResponse(
                entity.getId(),
                entity.getName(),
                entity.getGradeOrder(),
                entity.getDateCreated()
        );
    }
}
