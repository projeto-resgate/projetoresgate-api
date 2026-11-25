package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateClassCollaboratorCommand(
        @JsonIgnore
        UUID id,
        @JsonIgnore
        UUID classGroupId,
        @NotNull(message = "O tipo de educador é obrigatório")
        UUID educatorCategoryItemId,
        Boolean active
) {
    public UpdateClassCollaboratorCommand withRoute(UUID classGroupId, UUID id) {
        return new UpdateClassCollaboratorCommand(id, classGroupId, this.educatorCategoryItemId, this.active);
    }
}
