package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.SoftDeleteLegalPersonCommand;

public interface SoftDeleteLegalPersonUseCase {
    void handle(SoftDeleteLegalPersonCommand command);
}