package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.usecase.command.SoftDeleteUserCommand;

public interface SoftDeleteUserUseCase {

    void handle(SoftDeleteUserCommand cmd);

}
