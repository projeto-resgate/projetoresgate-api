package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;

import java.util.UUID;

public interface CreateUserUseCase {

    UUID handle(CreateUserCommand cmd);

}
