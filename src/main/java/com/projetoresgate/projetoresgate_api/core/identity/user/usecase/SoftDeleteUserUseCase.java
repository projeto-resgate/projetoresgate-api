package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.SoftDeleteUserCommand;

public interface SoftDeleteUserUseCase {

    void handle(SoftDeleteUserCommand cmd);
}
