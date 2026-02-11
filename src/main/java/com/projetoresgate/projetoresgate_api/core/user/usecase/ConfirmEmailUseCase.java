package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.usecase.command.ConfirmEmailCommand;

public interface ConfirmEmailUseCase {
    void handle(ConfirmEmailCommand command);
}
