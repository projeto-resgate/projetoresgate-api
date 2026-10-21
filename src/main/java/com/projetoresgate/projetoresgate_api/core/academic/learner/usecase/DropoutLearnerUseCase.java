package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.DropoutLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.DropoutLearnerCommand;

public interface DropoutLearnerUseCase {
    DropoutLearnerResponse handle(DropoutLearnerCommand command);
}
