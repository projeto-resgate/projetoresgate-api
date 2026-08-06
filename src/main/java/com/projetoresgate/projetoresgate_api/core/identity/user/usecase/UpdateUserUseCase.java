package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.UpdateUserCommand;

public interface UpdateUserUseCase {

    void handle(UpdateUserCommand cmd);
}
