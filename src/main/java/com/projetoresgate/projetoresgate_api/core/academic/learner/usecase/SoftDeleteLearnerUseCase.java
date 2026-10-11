package com.projetoresgate.projetoresgate_api.core.academic.learner.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.SoftDeleteLearnerCommand;

public interface SoftDeleteLearnerUseCase {
    void handle(SoftDeleteLearnerCommand command);
}
