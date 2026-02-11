package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.SoftDeleteUserUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.SoftDeleteUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
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
