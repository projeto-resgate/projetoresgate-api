package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.SoftDeleteSchoolGradeUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard.Dependency.of;

@Service
public class SoftDeleteSchoolGradeService implements SoftDeleteSchoolGradeUseCase {

    private final SchoolGradeRepository repository;

    public SoftDeleteSchoolGradeService(SchoolGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteSchoolGradeCommand command) {
        SchoolGrade schoolGrade = repository.findByIdOrThrow(command.id());

        SchoolGradeRepository.SchoolGradeDependencies dependencies = repository.countDependencies(schoolGrade.getId());
        DeletionGuard.ensureNoDependencies("a série escolar",
                of(dependencies.getClassGroups(), "turma", "turmas"),
                of(dependencies.getLearners(), "educando", "educandos"),
                of(dependencies.getResponsibles(), "responsável", "responsáveis"));

        repository.delete(schoolGrade);
    }
}
