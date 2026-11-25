package com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRow;

import java.util.UUID;

public record CollaboratorSummaryResponse(
        UUID id,
        UUID naturalPersonId,
        String naturalPersonName,
        UUID legalPersonId,
        String legalPersonName,
        boolean isVoluntary
) {
    public static CollaboratorSummaryResponse fromRow(CollaboratorRow row) {
        return new CollaboratorSummaryResponse(
                row.getId(),
                row.getNaturalPersonId(),
                row.getNaturalPersonName(),
                row.getLegalPersonId(),
                row.getLegalPersonName(),
                row.getIsVoluntary()
        );
    }
}
