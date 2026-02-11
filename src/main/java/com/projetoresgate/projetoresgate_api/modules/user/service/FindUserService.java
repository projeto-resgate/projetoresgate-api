package com.projetoresgate.projetoresgate_api.modules.user.service;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.modules.user.domain.User;
import com.projetoresgate.projetoresgate_api.modules.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.FindUserUseCase;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.query.FindUserByIdQuery;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FindUserService implements FindUserUseCase {

    private final UserRepository repository;

    public FindUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User handle(FindUserByIdQuery query) {
        Optional<User> optionalUser = repository.findById(query.id());
        if (optionalUser.isEmpty()) {
            throw new InternalException("Usuário não encontrado.");
        }
        return optionalUser.get();
    }
}
