package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;

import java.util.UUID;

public record LegalPersonSummaryResponse(
        UUID id,
        String displayName,
        String cnpj
) {
    public static LegalPersonSummaryResponse fromEntity(LegalPerson entity) {
        return new LegalPersonSummaryResponse(
                entity.getId(),
                entity.getDisplayName(),
                entity.getCnpj()
        );
    }
}