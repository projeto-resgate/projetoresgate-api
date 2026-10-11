package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ScholarshipCondition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateLearnerCommand(
        @JsonIgnore
        UUID id,
        @NotNull(message = "A pessoa física é obrigatória")
        UUID naturalPersonId,
        @NotNull(message = "O grupo familiar é obrigatório")
        UUID familyGroupId,
        UUID institutionId,
        UUID schoolGradeId,
        Boolean interestArts,
        ScholarshipCondition scholarshipCondition,
        Integer academicYear,
        @Size(max = 255, message = "O nome da mãe não pode exceder 255 caracteres")
        String motherName,
        @Size(max = 255, message = "A matrícula escolar não pode exceder 255 caracteres")
        String schoolEnrollment,
        @Valid
        @NotNull(message = "A lista de responsáveis é obrigatória")
        List<ResponsibleCommand> responsibleList
) {
    public UpdateLearnerCommand withId(UUID id) {
        return new UpdateLearnerCommand(id, this.naturalPersonId, this.familyGroupId, this.institutionId,
                this.schoolGradeId, this.interestArts, this.scholarshipCondition, this.academicYear,
                this.motherName, this.schoolEnrollment, this.responsibleList);
    }
}