package com.projetoresgate.projetoresgate_api.core.academic.program.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;

public record EducatorCategoryItemNameResponse(
        String name
) {
    public static EducatorCategoryItemNameResponse fromEntity(EducatorCategoryItem entity) {
        return new EducatorCategoryItemNameResponse(entity.getName());
    }
}