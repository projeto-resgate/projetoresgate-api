package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateClassGroupCommand(
        @NotNull(message = "O programa é obrigatório")
        UUID programId,
        @NotNull(message = "A instituição é obrigatória")
        UUID institutionId,
        List<UUID> schoolGradeIds,
        @NotNull(message = "A previsão de alunos é obrigatória")
        @PositiveOrZero(message = "A previsão de alunos não pode ser negativa")
        Integer studentProjections,
        @NotNull(message = "A data de início é obrigatória")
        LocalDateTime startDate,
        @NotNull(message = "A duração do período letivo é obrigatória")
        @Min(value = 1, message = "A duração do período letivo deve ser de pelo menos 1 mês")
        Integer durationInMonths,
        @Size(max = 2000, message = "A observação não pode exceder 2000 caracteres")
        String note
) {
}
