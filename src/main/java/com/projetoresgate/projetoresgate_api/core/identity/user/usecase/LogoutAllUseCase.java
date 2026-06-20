package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutAllCommand;

public interface LogoutAllUseCase {

    void handle(LogoutAllCommand command);

}

