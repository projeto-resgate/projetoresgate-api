package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.UUID;

import static java.util.Objects.isNull;

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
                isNull(naturalPerson) ? null : naturalPerson.getName(),
                entity.getLegalPersonId(),
                isNull(legalPerson) ? null : legalPerson.getName(),
                entity.isVoluntary()
        );
    }
}
