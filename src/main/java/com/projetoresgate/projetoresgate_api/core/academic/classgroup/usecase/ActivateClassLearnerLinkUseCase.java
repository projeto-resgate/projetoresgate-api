package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.ActivateClassLearnerLinkCommand;

public interface ActivateClassLearnerLinkUseCase {

    void handle(ActivateClassLearnerLinkCommand command);
}
