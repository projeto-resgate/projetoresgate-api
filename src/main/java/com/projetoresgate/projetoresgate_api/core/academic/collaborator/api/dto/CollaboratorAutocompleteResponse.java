package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRow;

import java.util.UUID;

public record CollaboratorAutocompleteResponse(
        UUID id,
        UUID naturalPersonId,
        String naturalPersonName,
        String naturalPersonCpf,
        UUID legalPersonId,
        String legalPersonName,
        boolean isVoluntary
) {
    public static CollaboratorAutocompleteResponse fromRow(CollaboratorRow row) {
        return new CollaboratorAutocompleteResponse(
                row.getId(),
                row.getNaturalPersonId(),
                row.getNaturalPersonName(),
                row.getNaturalPersonCpf(),
                row.getLegalPersonId(),
                row.getLegalPersonName(),
                row.getIsVoluntary()
        );
    }
}
