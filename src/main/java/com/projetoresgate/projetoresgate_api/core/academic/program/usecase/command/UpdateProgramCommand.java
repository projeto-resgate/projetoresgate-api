package com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateProgramCommand(
        @JsonIgnore
        UUID id,
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
        String name,
        String webSiteUrl,
        @NotNull(message = "O status é obrigatório")
        ProgramStatus status,
        UUID institutionId,
        @Valid
        List<UpdateEducatorCategoryItemCommand> educatorCategoryItems
) {
    public UpdateProgramCommand withId(UUID id) {
        return new UpdateProgramCommand(id, this.name, this.webSiteUrl, this.status, this.institutionId, this.educatorCategoryItems);
    }

    public record UpdateEducatorCategoryItemCommand(
            UUID id,
            @NotBlank(message = "O nome da categoria é obrigatório")
            @Size(max = 255, message = "O nome da categoria não pode exceder 255 caracteres")
            String name
    ) {
    }
}
