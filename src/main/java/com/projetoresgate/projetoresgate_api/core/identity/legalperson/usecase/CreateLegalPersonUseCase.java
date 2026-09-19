package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.CreateLegalPersonCommand;

public interface CreateLegalPersonUseCase {
    LegalPerson handle(CreateLegalPersonCommand command);
}
