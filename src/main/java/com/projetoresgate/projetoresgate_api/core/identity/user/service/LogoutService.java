package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.LogoutUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutUseCase {

    private final IRefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public LogoutService(IRefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(LogoutCommand command) {
        refreshTokenService.revokeRefreshToken(command.refreshToken())
                .ifPresent(user -> {
                    user.invalidateTokens();
                    userRepository.save(user);
                });
    }
}
