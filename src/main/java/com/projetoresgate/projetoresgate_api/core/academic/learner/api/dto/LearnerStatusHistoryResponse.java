package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.LearnerStatusHistory;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LearnerStatusHistoryResponse(
        UUID id,
        LearnerStatusEventType type,
        String reason,
        LocalDate eventDate,
        LocalDateTime registeredAt
) {
    public static LearnerStatusHistoryResponse fromEntity(LearnerStatusHistory entity) {
        return new LearnerStatusHistoryResponse(
                entity.getId(),
                entity.getType(),
                entity.getReason(),
                entity.getEventDate(),
                entity.getDateCreated()
        );
    }
}
