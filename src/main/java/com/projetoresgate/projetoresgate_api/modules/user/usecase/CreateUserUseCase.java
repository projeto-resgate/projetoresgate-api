package com.projetoresgate.projetoresgate_api.modules.user.usecase;

import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.CreateUserCommand;

import java.util.UUID;

public interface CreateUserUseCase {

    UUID handle(CreateUserCommand cmd);

}
