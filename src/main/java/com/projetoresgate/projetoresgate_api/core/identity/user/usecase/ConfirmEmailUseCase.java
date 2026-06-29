package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.ConfirmEmailCommand;

public interface ConfirmEmailUseCase {
    void handle(ConfirmEmailCommand command);
}
