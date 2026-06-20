package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.LogoutUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.services.RefreshTokenService;
import org.springframework.stereotype.Service;

@Service
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenService refreshTokenService;

    public LogoutService(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void handle(LogoutCommand command) {
        refreshTokenService.revokeRefreshToken(command.refreshToken());
    }
}

