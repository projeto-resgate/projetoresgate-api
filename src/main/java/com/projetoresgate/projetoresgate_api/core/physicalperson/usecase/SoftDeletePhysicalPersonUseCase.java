package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.SoftDeletePhysicalPersonCommand;

public interface SoftDeletePhysicalPersonUseCase {
    void handle(SoftDeletePhysicalPersonCommand command);
}
