package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

public record FamilyGroupNaturalPersonResponse(
        UUID id,
        String name,
        String rg,
        String cpf,
        String cellphone,
        List<NaturalPersonRoleType> roles
) {
    public static FamilyGroupNaturalPersonResponse fromEntity(NaturalPerson entity, List<NaturalPersonRoleType> roles) {
        return new FamilyGroupNaturalPersonResponse(
                entity.getId(),
                entity.getName(),
                entity.getRg(),
                entity.getCpf(),
                entity.getCellphone(),
                isNull(roles) ? List.of() : List.copyOf(roles)
        );
    }
}