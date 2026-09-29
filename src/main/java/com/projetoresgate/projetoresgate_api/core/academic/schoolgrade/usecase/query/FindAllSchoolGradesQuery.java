package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query;

import org.springframework.data.domain.Pageable;

public record FindAllSchoolGradesQuery(
        String name,
        Integer gradeOrder,
        Pageable pageable
) {
}
