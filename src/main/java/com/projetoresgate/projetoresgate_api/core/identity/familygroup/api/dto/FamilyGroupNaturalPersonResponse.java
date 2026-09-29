package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.UUID;

public record FamilyGroupNaturalPersonResponse(
        UUID id,
        String name,
        String rg,
        String cpf,
        String cellphone
) {
    public static FamilyGroupNaturalPersonResponse fromEntity(NaturalPerson entity) {
        return new FamilyGroupNaturalPersonResponse(
                entity.getId(),
                entity.getName(),
                entity.getRg(),
                entity.getCpf(),
                entity.getCellphone()
        );
    }
}
