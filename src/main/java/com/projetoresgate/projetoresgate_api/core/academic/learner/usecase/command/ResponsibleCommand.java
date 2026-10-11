package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ResponsibleCommand(
        @NotNull(message = "A pessoa física do responsável é obrigatória")
        UUID naturalPersonId,
        @NotNull(message = "O tipo do responsável é obrigatório")
        ResponsibleType type,
        @NotNull(message = "O parentesco do responsável é obrigatório")
        Kinship kinship,
        UUID schoolGradeId
) {

    public Responsible toEntity(Learner learner) {
        return Responsible.create(learner, naturalPersonId, type, kinship, schoolGradeId);
    }
}