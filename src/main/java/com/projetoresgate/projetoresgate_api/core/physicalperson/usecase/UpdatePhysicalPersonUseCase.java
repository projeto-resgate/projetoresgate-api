package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.UpdatePhysicalPersonCommand;

public interface UpdatePhysicalPersonUseCase {
    PhysicalPerson handle(UpdatePhysicalPersonCommand command);
}
