package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassLearnerCommand;

public interface SoftDeleteClassLearnerUseCase {
    void handle(SoftDeleteClassLearnerCommand command);
}
