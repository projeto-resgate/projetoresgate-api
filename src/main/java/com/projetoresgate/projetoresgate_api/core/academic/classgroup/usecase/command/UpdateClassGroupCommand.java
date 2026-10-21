package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateClassGroupCommand(
        @JsonIgnore
        UUID id,
        UUID programId,
        UUID institutionId,
        List<UUID> schoolGradeIds,
        Integer studentProjections,
        LocalDateTime startDate,
        Integer durationInMonths,
        String note,
        Boolean active
) {
    public UpdateClassGroupCommand withId(UUID id) {
        return new UpdateClassGroupCommand(id, this.programId, this.institutionId, this.schoolGradeIds,
                this.studentProjections, this.startDate, this.durationInMonths, this.note, this.active);
    }
}
