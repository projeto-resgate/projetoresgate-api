package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record UpdateClassCollaboratorCommand(
        @JsonIgnore
        UUID id,
        @JsonIgnore
        UUID classGroupId,
        UUID educatorCategoryItemId,
        Boolean active
) {
    public UpdateClassCollaboratorCommand withRoute(UUID classGroupId, UUID id) {
        return new UpdateClassCollaboratorCommand(id, classGroupId, this.educatorCategoryItemId, this.active);
    }
}
