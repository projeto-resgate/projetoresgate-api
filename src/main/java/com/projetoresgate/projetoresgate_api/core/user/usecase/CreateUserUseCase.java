package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;

public interface CreateUserUseCase {

    User handle(CreateUserCommand cmd);

}
