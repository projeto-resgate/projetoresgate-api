package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.UUID;

public record CollaboratorSummaryResponse(
        UUID id,
        UUID naturalPersonId,
        String naturalPersonName,
        UUID legalPersonId,
        String legalPersonName,
        boolean isVoluntary
) {
    public static CollaboratorSummaryResponse fromEntity(Collaborator entity, NaturalPerson naturalPerson,
                                                         LegalPerson legalPerson) {
        return new CollaboratorSummaryResponse(
                entity.getId(),
                entity.getNaturalPersonId(),
                naturalPerson == null ? null : naturalPerson.getName(),
                entity.getLegalPersonId(),
                legalPerson == null ? null : legalPerson.getDisplayName(),
                entity.isVoluntary()
        );
    }
}
