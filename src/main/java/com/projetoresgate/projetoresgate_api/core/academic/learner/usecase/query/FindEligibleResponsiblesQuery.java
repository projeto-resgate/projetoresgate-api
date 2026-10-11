package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindEligibleResponsiblesQuery(
        UUID familyGroupId,
        UUID excludeNaturalPersonId,
        Pageable pageable
) {
}
