package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerAutocompleteRow;

import java.util.UUID;

public record LearnerAutocompleteResponse(
        UUID id,
        String name,
        String cpf,
        boolean active
) {
    public static LearnerAutocompleteResponse fromRow(LearnerAutocompleteRow row) {
        return new LearnerAutocompleteResponse(
                row.getId(),
                row.getName(),
                row.getCpf(),
                LearnerStatus.ACTIVE.name().equals(row.getStatus())
        );
    }
}
