package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.UpdateLegalPersonCommand;

public interface UpdateLegalPersonUseCase {
    LegalPerson handle(UpdateLegalPersonCommand command);
}
