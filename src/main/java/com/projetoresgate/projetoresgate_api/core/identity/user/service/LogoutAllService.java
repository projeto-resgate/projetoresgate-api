package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.LogoutAllUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutAllCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class LogoutAllService implements LogoutAllUseCase {

    private final IRefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public LogoutAllService(IRefreshTokenService refreshTokenService, UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(LogoutAllCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        refreshTokenService.revokeAllUserTokens(user);
        user.invalidateTokens();
        userRepository.save(user);
    }
}

