package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.FindAllSchoolGradesUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindAllSchoolGradesQuery;
import com.projetoresgate.projetoresgate_api.shared.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class FindAllSchoolGradesService implements FindAllSchoolGradesUseCase {

    private final SchoolGradeRepository repository;

    public FindAllSchoolGradesService(SchoolGradeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SchoolGradeResponse> handle(FindAllSchoolGradesQuery query) {

        Specification<SchoolGrade> filters = new SpecificationBuilder<SchoolGrade>()
                .with("name", "~", query.name())
                .with("gradeOrder", ":", query.gradeOrder())
                .build();

        Page<SchoolGrade> page = repository.findAll(filters, query.pageable());

        return new PageImpl<>(
                toResponses(page.getContent()),
                page.getPageable(),
                page.getTotalElements()
        );
    }

    private List<SchoolGradeResponse> toResponses(List<SchoolGrade> schoolGrades) {
        if (schoolGrades.isEmpty()) {
            return Collections.emptyList();
        }
        return schoolGrades.stream().map(SchoolGradeResponse::fromEntity).toList();
    }
}
