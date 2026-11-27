package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.LearnerClassGroupRow;

import java.util.UUID;

public record LearnerClassGroupResponse(
        UUID id,
        String title
) {
    public static LearnerClassGroupResponse fromRow(LearnerClassGroupRow row) {
        return new LearnerClassGroupResponse(row.getClassGroupId(), row.getTitle());
    }
}
