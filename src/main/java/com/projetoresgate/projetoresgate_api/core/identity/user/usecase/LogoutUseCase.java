package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutCommand;

public interface LogoutUseCase {

    void handle(LogoutCommand command);

}

