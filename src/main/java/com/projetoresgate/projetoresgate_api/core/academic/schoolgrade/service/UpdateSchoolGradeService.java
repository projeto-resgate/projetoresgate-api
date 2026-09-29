package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.UpdateSchoolGradeUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UpdateSchoolGradeService implements UpdateSchoolGradeUseCase {

    private final SchoolGradeRepository repository;

    public UpdateSchoolGradeService(SchoolGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SchoolGrade handle(UpdateSchoolGradeCommand command) {
        SchoolGrade schoolGrade = repository.findByIdOrThrow(command.id());

        if (StringUtils.hasText(command.name()) && repository.existsByNameAndIdNot(command.name(), schoolGrade.getId())) {
            throw new InternalException("Já existe uma série escolar cadastrada com este nome.");
        }

        return repository.save(schoolGrade.update()
                .name(command.name())
                .gradeOrder(command.gradeOrder())
                .apply());
    }
}
