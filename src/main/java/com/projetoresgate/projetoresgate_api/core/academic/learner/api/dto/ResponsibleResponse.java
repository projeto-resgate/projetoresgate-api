package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;

import java.util.UUID;

public record ResponsibleResponse(
        UUID id,
        UUID naturalPersonId,
        String name,
        ResponsibleType type,
        Kinship kinship,
        UUID schoolGradeId
) {
    public static ResponsibleResponse fromEntity(Responsible entity, String name) {
        return new ResponsibleResponse(
                entity.getId(),
                entity.getNaturalPersonId(),
                name,
                entity.getType(),
                entity.getKinship(),
                entity.getSchoolGradeId()
        );
    }
}