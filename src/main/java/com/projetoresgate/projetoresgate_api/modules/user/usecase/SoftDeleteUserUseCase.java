package com.projetoresgate.projetoresgate_api.modules.user.usecase;

import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.SoftDeleteUserCommand;

public interface SoftDeleteUserUseCase {

    void handle(SoftDeleteUserCommand cmd);

}
