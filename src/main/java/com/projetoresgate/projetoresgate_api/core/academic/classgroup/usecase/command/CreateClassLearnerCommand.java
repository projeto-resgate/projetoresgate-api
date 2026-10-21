package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClassLearnerCommand(
        @JsonIgnore
        UUID classGroupId,
        @NotNull(message = "O educando é obrigatório")
        UUID learnerId
) {
    public CreateClassLearnerCommand withClassGroupId(UUID classGroupId) {
        return new CreateClassLearnerCommand(classGroupId, this.learnerId);
    }
}
