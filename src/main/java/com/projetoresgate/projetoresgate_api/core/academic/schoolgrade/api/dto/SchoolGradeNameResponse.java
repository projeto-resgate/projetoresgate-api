package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;

import java.util.UUID;

public record SchoolGradeNameResponse(
        UUID id,
        String name,
        Integer gradeOrder
) {
    public static SchoolGradeNameResponse fromEntity(SchoolGrade entity) {
        return new SchoolGradeNameResponse(
                entity.getId(),
                entity.getName(),
                entity.getGradeOrder()
        );
    }
}
