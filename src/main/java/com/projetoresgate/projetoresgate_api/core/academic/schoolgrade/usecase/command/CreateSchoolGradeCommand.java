package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSchoolGradeCommand(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
        String name,
        @NotNull(message = "A ordem é obrigatória")
        @Min(value = 1, message = "A ordem deve ser maior que zero")
        Integer gradeOrder
) {
}
