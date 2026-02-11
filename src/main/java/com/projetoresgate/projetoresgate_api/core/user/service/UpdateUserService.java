package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.UpdateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.UpdateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UpdateUserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void handle(UpdateUserCommand cmd) {
        User user = repository.findById(cmd.id()).orElseThrow(() -> new InternalException("Usuário não encontrado."));

        if (cmd.name() != null && !cmd.name().isBlank()) {
            user.setName(cmd.name());
        }

        if (cmd.password() != null && !cmd.password().isBlank()) {
            if (cmd.currentPassword() == null || cmd.currentPassword().isBlank()) {
                throw new InternalException("A senha atual é obrigatória para alterar a senha.");
            }

            if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
                throw new InternalException("A senha atual está incorreta.");
            }

            user.setPassword(passwordEncoder.encode(cmd.password()));
        }

        user.validate();
        repository.save(user);
    }
}
