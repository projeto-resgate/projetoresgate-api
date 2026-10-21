package com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto;

public record DropoutLearnerResponse(
        LearnerResponse learner,
        boolean classLinksDeactivated,
        int deactivatedClassLinks
) {
    public static DropoutLearnerResponse fromEntity(LearnerResponse learner, int deactivatedClassLinks) {
        return new DropoutLearnerResponse(learner, deactivatedClassLinks > 0, deactivatedClassLinks);
    }
}
