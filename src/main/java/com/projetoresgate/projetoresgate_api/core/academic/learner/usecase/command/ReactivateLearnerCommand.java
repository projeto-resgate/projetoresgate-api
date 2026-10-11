package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record ReactivateLearnerCommand(
        @JsonIgnore
        UUID id,
        String note
) {
    public ReactivateLearnerCommand withId(UUID id) {
        return new ReactivateLearnerCommand(id, this.note);
    }
}
