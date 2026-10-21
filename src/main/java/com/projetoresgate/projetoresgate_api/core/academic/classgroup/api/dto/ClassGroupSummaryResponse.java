package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;

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
    public static ClassGroupSummaryResponse fromEntity(ClassGroup entity, String programName, String institutionName) {
        return new ClassGroupSummaryResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getProgramId(),
                programName,
                entity.getInstitutionId(),
                institutionName,
                entity.isActive()
        );
    }
}
