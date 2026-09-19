package com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query;

import java.util.UUID;

public record FindEducatorCategoryItemNamesQuery(
        UUID programId,
        String name
) {
}