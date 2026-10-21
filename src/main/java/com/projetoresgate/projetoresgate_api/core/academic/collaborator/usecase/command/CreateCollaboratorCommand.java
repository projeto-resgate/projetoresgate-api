package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCollaboratorCommand(
        @NotNull(message = "A pessoa física é obrigatória")
        UUID naturalPersonId,
        UUID legalPersonId,
        @NotNull(message = "A informação de voluntário é obrigatória")
        Boolean isVoluntary
) {
}
