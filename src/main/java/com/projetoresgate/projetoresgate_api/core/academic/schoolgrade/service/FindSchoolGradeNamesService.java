package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.FindSchoolGradeNamesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeNamesQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindSchoolGradeNamesService implements FindSchoolGradeNamesUseCase {

    private final SchoolGradeRepository repository;

    public FindSchoolGradeNamesService(SchoolGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolGradeNameResponse> handle(FindSchoolGradeNamesQuery query) {

        Specification<SchoolGrade> nameFilter = new SpecificationBuilder<SchoolGrade>()
                .with("name", "~", query.name())
                .build();

        Pageable pageable = PageRequest.of(0, query.limit(), Sort.by("gradeOrder").ascending());

        return repository.findAll(nameFilter, pageable).getContent().stream()
                .map(SchoolGradeNameResponse::fromEntity)
                .toList();
    }
}
