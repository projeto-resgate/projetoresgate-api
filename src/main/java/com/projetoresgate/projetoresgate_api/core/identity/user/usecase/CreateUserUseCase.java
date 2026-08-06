package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.CreateUserCommand;

public interface CreateUserUseCase {

    User handle(CreateUserCommand cmd);
}
