package com.projetoresgate.projetoresgate_api.core.academic.program.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.EducatorCategoryItemNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindEducatorCategoryItemNamesQuery;

import java.util.List;

public interface FindEducatorCategoryItemNamesUseCase {
    List<EducatorCategoryItemNameResponse> handle(FindEducatorCategoryItemNamesQuery query);
}