package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query;

public record FindSchoolGradeNamesQuery(
        String name,
        int limit
) {
}
