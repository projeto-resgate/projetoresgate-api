package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.UpdateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.UpdateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static java.util.Objects.nonNull;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenService refreshTokenService;

    public UpdateUserService(UserRepository repository, PasswordEncoder passwordEncoder,
                             IRefreshTokenService refreshTokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public void handle(UpdateUserCommand cmd) {
        User user = repository.findById(cmd.id())
                .orElseThrow(() -> new InternalException("Usuário não encontrado."));

        user.update()
                .name(cmd.name())
                .nickname(cmd.nickname())
                .apply();

        boolean passwordChanged = false;
        if (nonNull(cmd.password()) && !cmd.password().isBlank()) {
            if (!StringUtils.hasText(cmd.currentPassword())) {
                throw new InternalException("A senha atual é obrigatória para alterar a senha.");
            }

            if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
                throw new InternalException("A senha atual está incorreta.");
            }

            if (cmd.password().length() < 6) {
                throw new InternalException("A nova senha deve ter no mínimo 6 caracteres.");
            }

            user.changePassword(passwordEncoder.encode(cmd.password()));
            passwordChanged = true;
        }

        user.validate();
        repository.save(user);
        if (passwordChanged) {
            refreshTokenService.revokeAllUserTokens(user);
        }
    }
}
