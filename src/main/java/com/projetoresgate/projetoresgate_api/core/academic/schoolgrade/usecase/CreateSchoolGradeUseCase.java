package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.CreateSchoolGradeCommand;

public interface CreateSchoolGradeUseCase {
    SchoolGrade handle(CreateSchoolGradeCommand command);
}
