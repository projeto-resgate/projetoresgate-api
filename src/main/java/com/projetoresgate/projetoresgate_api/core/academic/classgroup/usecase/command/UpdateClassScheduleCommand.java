package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record UpdateClassScheduleCommand(
        @JsonIgnore
        UUID id,
        @JsonIgnore
        UUID classGroupId,
        @NotNull(message = "O dia da semana é obrigatório")
        WeekDay weekDay,
        @NotNull(message = "O horário de início é obrigatório")
        LocalTime startTime,
        @NotNull(message = "O horário de término é obrigatório")
        LocalTime endTime,
        Boolean active
) {
    public UpdateClassScheduleCommand withRoute(UUID classGroupId, UUID id) {
        return new UpdateClassScheduleCommand(id, classGroupId, this.weekDay, this.startTime, this.endTime,
                this.active);
    }
}
