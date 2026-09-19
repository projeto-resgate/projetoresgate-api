package com.projetoresgate.projetoresgate_api.core.academic.program.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;

import java.util.UUID;

public record EducatorCategoryItemResponse(
        UUID id,
        String name
) {
    public static EducatorCategoryItemResponse fromEntity(EducatorCategoryItem entity) {
        return new EducatorCategoryItemResponse(
                entity.getId(),
                entity.getName()
        );
    }
}