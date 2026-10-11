package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.UpdateLearnerCommand;

public interface UpdateLearnerUseCase {
    LearnerResponse handle(UpdateLearnerCommand command);
}