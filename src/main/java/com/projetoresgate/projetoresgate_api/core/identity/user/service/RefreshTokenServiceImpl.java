package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.RefreshTokenResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.RefreshTokenUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.RefreshTokenQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenUseCase {

    private final IRefreshTokenService refreshTokenService;
    private final ITokenService tokenService;

    public RefreshTokenServiceImpl(IRefreshTokenService refreshTokenService, ITokenService tokenService) {
        this.refreshTokenService = refreshTokenService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public RefreshTokenResponse handle(RefreshTokenQuery query) {
        User user = refreshTokenService.validateRefreshToken(query.refreshToken());

        String accessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user);
        refreshTokenService.revokeRefreshToken(query.refreshToken());

        return new RefreshTokenResponse(
                accessToken,
                newRefreshToken,
                tokenService.getAccessTokenDurationSeconds(),
                "Bearer"
        );
    }
}
