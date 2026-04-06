package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.CreateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;

    public CreateUserService(UserRepository repository, PasswordEncoder passwordEncoder, RequestEmailConfirmationUseCase requestEmailConfirmationUseCase) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
    }

    public User handle(CreateUserCommand cmd) {
        if (repository.findByEmail(cmd.email()).isPresent()) {
            throw new InternalException("Este e-mail já está cadastrado.");
        }

        String encodedPassword = null;
        if (StringUtils.hasText(cmd.password())) {
            if (cmd.password().length() < 6) {
                throw new InternalException("A senha deve ter no mínimo 6 caracteres.");
            }
            encodedPassword = passwordEncoder.encode(cmd.password());
        }

        User newUser = User.create(
                cmd.email(),
                encodedPassword,
                cmd.name(),
                cmd.nickname()
        );

        newUser = repository.save(newUser);

        requestEmailConfirmationUseCase.handle(newUser.getEmail());

        return newUser;
    }

}
