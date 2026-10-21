package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateCollaboratorCommand(
        @JsonIgnore
        UUID id,
        @NotNull(message = "A pessoa física é obrigatória")
        UUID naturalPersonId,
        UUID legalPersonId,
        @NotNull(message = "A informação de voluntário é obrigatória")
        Boolean isVoluntary
) {
    public UpdateCollaboratorCommand withId(UUID id) {
        return new UpdateCollaboratorCommand(id, this.naturalPersonId, this.legalPersonId, this.isVoluntary);
    }
}
