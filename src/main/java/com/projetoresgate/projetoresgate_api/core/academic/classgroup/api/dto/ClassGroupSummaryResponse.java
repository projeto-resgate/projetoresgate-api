package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRow;

import java.util.UUID;

public record ClassGroupSummaryResponse(
        UUID id,
        String title,
        UUID programId,
        String programName,
        UUID institutionId,
        String institutionName,
        boolean active
) {
    public static ClassGroupSummaryResponse fromRow(ClassGroupRow row) {
        return new ClassGroupSummaryResponse(
                row.getId(),
                row.getTitle(),
                row.getProgramId(),
                row.getProgramName(),
                row.getInstitutionId(),
                row.getInstitutionName(),
                row.getActive()
        );
    }
}
