package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindAllSchoolGradesQuery;
import org.springframework.data.domain.Page;

public interface FindAllSchoolGradesUseCase {
    Page<SchoolGradeResponse> handle(FindAllSchoolGradesQuery query);
}
