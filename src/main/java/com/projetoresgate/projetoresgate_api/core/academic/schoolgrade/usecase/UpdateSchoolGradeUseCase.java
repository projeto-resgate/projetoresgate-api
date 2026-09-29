package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;

public interface UpdateSchoolGradeUseCase {
    SchoolGrade handle(UpdateSchoolGradeCommand command);
}
