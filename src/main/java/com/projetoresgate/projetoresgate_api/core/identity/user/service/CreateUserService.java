package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.CreateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User handle(CreateUserCommand cmd) {
        if (repository.findByEmail(cmd.email()).isPresent()) {
            throw new InternalException("Este e-mail já está cadastrado.");
        }

        User newUser = User.create(
                cmd.email(),
                passwordEncoder.encode(cmd.password()),
                cmd.name(),
                cmd.nickname()
        );

        return repository.save(newUser);
    }
}
