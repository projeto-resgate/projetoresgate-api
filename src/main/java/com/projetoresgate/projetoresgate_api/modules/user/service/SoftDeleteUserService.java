package com.projetoresgate.projetoresgate_api.modules.user.service;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.modules.user.domain.User;
import com.projetoresgate.projetoresgate_api.modules.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.SoftDeleteUserUseCase;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.command.SoftDeleteUserCommand;
import org.springframework.stereotype.Service;

@Service
public class SoftDeleteUserService implements SoftDeleteUserUseCase {

    private final UserRepository repository;

    public SoftDeleteUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void handle(SoftDeleteUserCommand cmd) {
        User user = repository.findById(cmd.id()).orElseThrow(() -> new InternalException("Usuário não encontrado."));
        repository.delete(user);
    }
}
