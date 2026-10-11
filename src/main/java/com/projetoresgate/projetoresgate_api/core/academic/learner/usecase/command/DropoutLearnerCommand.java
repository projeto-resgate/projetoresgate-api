package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record DropoutLearnerCommand(
        @JsonIgnore
        UUID id,
        @NotBlank(message = "O motivo da desistência é obrigatório.")
        String reason,
        LocalDate eventDate
) {
    public DropoutLearnerCommand withId(UUID id) {
        return new DropoutLearnerCommand(id, this.reason, this.eventDate);
    }
}
