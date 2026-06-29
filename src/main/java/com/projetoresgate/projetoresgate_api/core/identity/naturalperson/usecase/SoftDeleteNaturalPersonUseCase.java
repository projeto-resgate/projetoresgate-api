package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;

public interface SoftDeleteNaturalPersonUseCase {
    void handle(SoftDeleteNaturalPersonCommand command);
}
