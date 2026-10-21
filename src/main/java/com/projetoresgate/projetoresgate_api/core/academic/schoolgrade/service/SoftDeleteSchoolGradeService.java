package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.RefreshClassGroupTitlesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.RefreshClassGroupTitlesCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.SoftDeleteSchoolGradeUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteSchoolGradeService implements SoftDeleteSchoolGradeUseCase {

    private final SchoolGradeRepository repository;
    private final RefreshClassGroupTitlesUseCase refreshClassGroupTitlesUseCase;

    public SoftDeleteSchoolGradeService(SchoolGradeRepository repository,
                                        RefreshClassGroupTitlesUseCase refreshClassGroupTitlesUseCase) {
        this.repository = repository;
        this.refreshClassGroupTitlesUseCase = refreshClassGroupTitlesUseCase;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteSchoolGradeCommand command) {
        SchoolGrade schoolGrade = repository.findByIdOrThrow(command.id());
        repository.delete(schoolGrade);
        refreshClassGroupTitlesUseCase.handle(RefreshClassGroupTitlesCommand.bySchoolGrade(schoolGrade.getId()));
    }
}
