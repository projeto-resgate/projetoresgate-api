package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassLearnerCommand;

public interface CreateClassLearnerUseCase {
    ClassLearnerResponse handle(CreateClassLearnerCommand command);
}
