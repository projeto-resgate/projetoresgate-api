package com.projetoresgate.projetoresgate_api.core.academic.program.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;

import java.util.UUID;

public record EducatorCategoryItemNameResponse(
        UUID id,
        String name
) {
    public static EducatorCategoryItemNameResponse fromEntity(EducatorCategoryItem entity) {
        return new EducatorCategoryItemNameResponse(
                entity.getId(),
                entity.getName()
        );
    }
}