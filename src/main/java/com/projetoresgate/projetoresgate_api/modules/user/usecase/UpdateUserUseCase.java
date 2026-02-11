package com.projetoresgate.projetoresgate_api.modules.user.usecase;

import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.UpdateUserCommand;

public interface UpdateUserUseCase {

    void handle(UpdateUserCommand cmd);

}
