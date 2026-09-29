package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.CreateSchoolGradeUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.CreateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSchoolGradeService implements CreateSchoolGradeUseCase {

    private final SchoolGradeRepository repository;

    public CreateSchoolGradeService(SchoolGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SchoolGrade handle(CreateSchoolGradeCommand command) {
        if (repository.existsByName(command.name())) {
            throw new InternalException("Já existe uma série escolar cadastrada com este nome.");
        }

        return repository.save(SchoolGrade.create(command.name(), command.gradeOrder()));
    }
}
