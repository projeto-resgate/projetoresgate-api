package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClassCollaboratorCommand(
        @JsonIgnore
        UUID classGroupId,
        @NotNull(message = "O colaborador é obrigatório")
        UUID collaboratorId,
        @NotNull(message = "O tipo de educador é obrigatório")
        UUID educatorCategoryItemId
) {
    public CreateClassCollaboratorCommand withClassGroupId(UUID classGroupId) {
        return new CreateClassCollaboratorCommand(classGroupId, this.collaboratorId, this.educatorCategoryItemId);
    }
}
