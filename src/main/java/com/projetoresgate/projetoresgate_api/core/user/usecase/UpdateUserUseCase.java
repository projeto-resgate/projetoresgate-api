package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.usecase.command.UpdateUserCommand;

public interface UpdateUserUseCase {

    void handle(UpdateUserCommand cmd);

}
