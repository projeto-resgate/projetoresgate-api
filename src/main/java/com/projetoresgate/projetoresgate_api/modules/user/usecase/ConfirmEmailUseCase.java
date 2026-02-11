package com.projetoresgate.projetoresgate_api.modules.user.usecase;

import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.ConfirmEmailCommand;

public interface ConfirmEmailUseCase {
    void handle(ConfirmEmailCommand command);
}
