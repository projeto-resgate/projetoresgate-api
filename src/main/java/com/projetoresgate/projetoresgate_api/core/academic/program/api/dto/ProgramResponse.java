package com.projetoresgate.projetoresgate_api.core.academic.program.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProgramResponse(
        UUID id,
        String name,
        String webSiteUrl,
        ProgramStatus status,
        LegalPersonSummaryResponse institution,
        List<EducatorCategoryItemResponse> educatorCategoryItems,
        LocalDateTime dateCreated
) {
    public static ProgramResponse fromEntity(Program entity, LegalPersonSummaryResponse institution) {
        List<EducatorCategoryItemResponse> items = entity.getEducatorCategoryItemList() == null
                ? List.of()
                : entity.getEducatorCategoryItemList().stream()
                .map(EducatorCategoryItemResponse::fromEntity)
                .toList();

        return new ProgramResponse(
                entity.getId(),
                entity.getName(),
                entity.getWebSiteUrl(),
                entity.getStatus(),
                institution,
                items,
                entity.getDateCreated()
        );
    }
}