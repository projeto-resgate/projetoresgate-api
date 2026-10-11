package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.UUID;

public record EligibleResponsibleResponse(
        UUID naturalPersonId,
        String name,
        String rg,
        String cpf,
        String cellphone
) {
    public static EligibleResponsibleResponse fromEntity(NaturalPerson entity) {
        return new EligibleResponsibleResponse(
                entity.getId(),
                entity.getName(),
                entity.getRg(),
                entity.getCpf(),
                entity.getCellphone()
        );
    }
}
