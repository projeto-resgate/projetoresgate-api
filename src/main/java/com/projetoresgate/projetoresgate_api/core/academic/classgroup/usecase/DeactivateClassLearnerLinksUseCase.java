package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.DeactivateClassLearnerLinksCommand;

public interface DeactivateClassLearnerLinksUseCase {

    int handle(DeactivateClassLearnerLinksCommand command);
}
