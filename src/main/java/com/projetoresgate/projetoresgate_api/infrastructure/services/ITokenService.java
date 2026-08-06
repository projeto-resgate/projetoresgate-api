package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;

import java.time.LocalDateTime;

public interface ITokenService {
    String generateToken(User user);
    String generateAccessToken(User user);
    String validateToken(String token);
    String validateAccessToken(String token);
    long getTokenVersion(String token);
    LocalDateTime getRefreshTokenExpiryDate();
    long getAccessTokenDurationSeconds();
}
