package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;

import java.util.UUID;

public record LearnerAutocompleteResponse(
        UUID id,
        String name,
        String cpf,
        boolean active
) {
    public static LearnerAutocompleteResponse fromEntity(Learner entity, String name, String cpf) {
        return new LearnerAutocompleteResponse(
                entity.getId(),
                name,
                cpf,
                entity.getStatus() == LearnerStatus.ACTIVE
        );
    }
}
