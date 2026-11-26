package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRow;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassCollaboratorResponse(
        UUID id,
        UUID classGroupId,
        UUID collaboratorId,
        String collaboratorName,
        String collaboratorCpf,
        String collaboratorRg,
        UUID educatorCategoryItemId,
        String educatorCategoryItemName,
        boolean active,
        LocalDateTime dateCreated
) {
    public static ClassCollaboratorResponse fromEntity(ClassCollaborator entity, String collaboratorName,
                                                       String collaboratorCpf, String collaboratorRg,
                                                       String educatorCategoryItemName) {
        return new ClassCollaboratorResponse(
                entity.getId(),
                entity.getClassGroup().getId(),
                entity.getCollaboratorId(),
                collaboratorName,
                collaboratorCpf,
                collaboratorRg,
                entity.getEducatorCategoryItemId(),
                educatorCategoryItemName,
                entity.isActive(),
                entity.getDateCreated()
        );
    }

    public static ClassCollaboratorResponse fromRow(ClassCollaboratorRow row) {
        return new ClassCollaboratorResponse(
                row.getId(),
                row.getClassGroupId(),
                row.getCollaboratorId(),
                row.getCollaboratorName(),
                row.getCollaboratorCpf(),
                row.getCollaboratorRg(),
                row.getEducatorCategoryItemId(),
                row.getEducatorCategoryItemName(),
                row.getActive(),
                row.getDateCreated()
        );
    }
}
