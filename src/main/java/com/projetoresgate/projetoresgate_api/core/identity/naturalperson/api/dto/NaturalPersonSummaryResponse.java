package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.UUID;

public record NaturalPersonSummaryResponse(
        UUID id,
        String name,
        String rg,
        String cpf
) {
    public static NaturalPersonSummaryResponse fromEntity(NaturalPerson entity) {
        return new NaturalPersonSummaryResponse(
                entity.getId(),
                entity.getName(),
                entity.getRg(),
                entity.getCpf()
        );
    }
}