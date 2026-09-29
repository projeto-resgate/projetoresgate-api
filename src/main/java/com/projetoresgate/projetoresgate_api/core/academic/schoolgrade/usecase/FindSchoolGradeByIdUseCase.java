package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeByIdQuery;

public interface FindSchoolGradeByIdUseCase {
    SchoolGrade handle(FindSchoolGradeByIdQuery query);
}
