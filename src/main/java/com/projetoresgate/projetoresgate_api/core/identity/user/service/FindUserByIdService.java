package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.FindUserByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.FindUserByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FindUserByIdService implements FindUserByIdUseCase {

    private final UserRepository repository;

    public FindUserByIdService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User handle(FindUserByIdQuery query) {
        return repository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }
}
