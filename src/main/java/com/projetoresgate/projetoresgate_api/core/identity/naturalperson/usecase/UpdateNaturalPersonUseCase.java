package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;

public interface UpdateNaturalPersonUseCase {
    NaturalPerson handle(UpdateNaturalPersonCommand command);
}
