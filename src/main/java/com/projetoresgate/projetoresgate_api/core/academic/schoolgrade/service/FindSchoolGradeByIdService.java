package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.FindSchoolGradeByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindSchoolGradeByIdService implements FindSchoolGradeByIdUseCase {

    private final SchoolGradeRepository repository;

    public FindSchoolGradeByIdService(SchoolGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolGrade handle(FindSchoolGradeByIdQuery query) {
        return repository.findByIdOrThrow(query.id());
    }
}
