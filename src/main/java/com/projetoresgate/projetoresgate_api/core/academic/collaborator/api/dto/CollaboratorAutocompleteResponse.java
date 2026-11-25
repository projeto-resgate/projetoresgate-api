package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.UUID;

import static java.util.Objects.isNull;

public record CollaboratorAutocompleteResponse(
        UUID id,
        UUID naturalPersonId,
        String naturalPersonName,
        String naturalPersonCpf,
        UUID legalPersonId,
        String legalPersonName,
        boolean isVoluntary
) {
    public static CollaboratorAutocompleteResponse fromEntity(Collaborator entity, NaturalPerson naturalPerson,
                                                              LegalPerson legalPerson) {
        return new CollaboratorAutocompleteResponse(
                entity.getId(),
                entity.getNaturalPersonId(),
                isNull(naturalPerson) ? null : naturalPerson.getName(),
                isNull(naturalPerson) ? null : naturalPerson.getCpf(),
                entity.getLegalPersonId(),
                isNull(legalPerson) ? null : legalPerson.getName(),
                entity.isVoluntary()
        );
    }
}
