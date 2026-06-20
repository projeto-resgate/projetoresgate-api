package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;

    public UserRegistrationService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   RequestEmailConfirmationUseCase requestEmailConfirmationUseCase) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
    }

    public User registerNewUser(CreateUserCommand command, UserRole initialRole) {
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new InternalException("Este e-mail já está cadastrado.");
        }

        String encodedPassword = nonNull(command.password()) ? passwordEncoder.encode(command.password()) : null;

        User newUser = User.create(command.email(), encodedPassword, command.name(), command.nickname());
        newUser.addRole(initialRole);

        User savedUser = userRepository.save(newUser);

        requestEmailConfirmationUseCase.handle(savedUser.getEmail());

        return savedUser;
    }
}