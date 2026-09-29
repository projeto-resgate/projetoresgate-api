package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;

public interface SoftDeleteSchoolGradeUseCase {
    void handle(SoftDeleteSchoolGradeCommand command);
}
