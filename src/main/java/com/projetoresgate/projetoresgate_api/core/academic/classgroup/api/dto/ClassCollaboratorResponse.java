package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassCollaboratorResponse(
        UUID id,
        UUID classGroupId,
        UUID collaboratorId,
        String collaboratorName,
        UUID educatorCategoryItemId,
        String educatorCategoryItemName,
        boolean active,
        LocalDateTime dateCreated
) {
    public static ClassCollaboratorResponse fromEntity(ClassCollaborator entity, String collaboratorName,
                                                       String educatorCategoryItemName) {
        return new ClassCollaboratorResponse(
                entity.getId(),
                entity.getClassGroup().getId(),
                entity.getCollaboratorId(),
                collaboratorName,
                entity.getEducatorCategoryItemId(),
                educatorCategoryItemName,
                entity.isActive(),
                entity.getDateCreated()
        );
    }
}
