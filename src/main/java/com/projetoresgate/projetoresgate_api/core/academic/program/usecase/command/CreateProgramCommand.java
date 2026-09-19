package com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateProgramCommand(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
        String name,
        String webSiteUrl,
        @NotNull(message = "O status é obrigatório")
        ProgramStatus status,
        UUID institutionId,
        @Valid
        List<CreateEducatorCategoryItemCommand> educatorCategoryItems
) {

    public record CreateEducatorCategoryItemCommand(
            @NotBlank(message = "O nome da categoria é obrigatório")
            @Size(max = 255, message = "O nome da categoria não pode exceder 255 caracteres")
            String name
    ) {
    }
}