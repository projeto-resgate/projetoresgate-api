package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;

import java.util.Optional;

public interface IRefreshTokenService {
    String createRefreshToken(User user);
    User validateRefreshToken(String plainTextToken);
    Optional<User> revokeRefreshToken(String plainTextToken);
    void revokeAllUserTokens(User user);
    void cleanupExpiredTokens();
    long countActiveTokens(User user);
}
