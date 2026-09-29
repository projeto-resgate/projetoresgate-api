package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public record UpdateSchoolGradeCommand(
        @JsonIgnore
        UUID id,
        String name,
        Integer gradeOrder
) {
    public UpdateSchoolGradeCommand withId(UUID id) {
        return new UpdateSchoolGradeCommand(id, this.name, this.gradeOrder);
    }
}
