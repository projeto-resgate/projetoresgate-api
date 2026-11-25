package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateSchoolGradeCommand(
        @JsonIgnore
        UUID id,
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
        String name,
        @Size(max = 50, message = "A abreviação não pode exceder 50 caracteres")
        String abbreviation,
        @NotNull(message = "A ordem é obrigatória")
        @Min(value = 1, message = "A ordem deve ser maior que zero")
        Integer gradeOrder
) {
    public UpdateSchoolGradeCommand withId(UUID id) {
        return new UpdateSchoolGradeCommand(id, this.name, this.abbreviation, this.gradeOrder);
    }
}
