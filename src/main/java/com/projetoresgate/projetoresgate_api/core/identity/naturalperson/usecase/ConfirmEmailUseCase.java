package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.ConfirmEmailCommand;

public interface ConfirmEmailUseCase {
    void handle(ConfirmEmailCommand command);
}
