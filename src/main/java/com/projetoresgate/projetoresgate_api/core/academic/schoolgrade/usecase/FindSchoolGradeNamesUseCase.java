package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeNamesQuery;

import java.util.List;

public interface FindSchoolGradeNamesUseCase {
    List<SchoolGradeNameResponse> handle(FindSchoolGradeNamesQuery query);
}
