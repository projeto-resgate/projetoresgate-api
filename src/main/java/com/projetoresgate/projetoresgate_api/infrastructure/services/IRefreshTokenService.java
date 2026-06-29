package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;

public interface IRefreshTokenService {
    String createRefreshToken(User user);
    User validateRefreshToken(String plainTextToken);
    void revokeRefreshToken(String plainTextToken);
    void revokeAllUserTokens(User user);
    void cleanupExpiredTokens();
    long countActiveTokens(User user);
}
