package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

public record CollaboratorResponse(
        UUID id,
        UUID naturalPersonId,
        String naturalPersonName,
        String naturalPersonCpf,
        UUID legalPersonId,
        String legalPersonName,
        boolean isVoluntary,
        LocalDateTime dateCreated
) {
    public static CollaboratorResponse fromEntity(Collaborator entity, NaturalPerson naturalPerson,
                                                  LegalPerson legalPerson) {
        return new CollaboratorResponse(
                entity.getId(),
                entity.getNaturalPersonId(),
                isNull(naturalPerson) ? null : naturalPerson.getName(),
                isNull(naturalPerson) ? null : naturalPerson.getCpf(),
                entity.getLegalPersonId(),
                isNull(legalPerson) ? null : legalPerson.getName(),
                entity.isVoluntary(),
                entity.getDateCreated()
        );
    }
}
