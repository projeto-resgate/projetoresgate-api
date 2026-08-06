package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.RefreshToken;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.RefreshTokenRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.TokenUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Objects.isNull;

@Service
public class RefreshTokenService implements IRefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final ITokenService tokenService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, ITokenService tokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String plainTextToken = TokenUtils.generateSecureToken();
        String tokenHash = TokenUtils.hashToken(plainTextToken);

        LocalDateTime expiryDate = tokenService.getRefreshTokenExpiryDate();

        RefreshToken refreshToken = new RefreshToken(tokenHash, user, expiryDate);
        refreshTokenRepository.save(refreshToken);

        return plainTextToken;
    }

    @Transactional(readOnly = true)
    public User validateRefreshToken(String plainTextToken) {
        if (isNull(plainTextToken) || plainTextToken.isBlank()) {
            throw new InternalException("Token de renovação inválido ou não fornecido.");
        }

        String tokenHash = TokenUtils.hashToken(plainTextToken);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (refreshTokenOpt.isEmpty()) {
            throw new InternalException("Token de renovação inválido ou não encontrado.");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.isExpired()) {
            throw new InternalException("Token de renovação expirou.");
        }

        return refreshToken.getUser();
    }

    @Transactional
    public Optional<User> revokeRefreshToken(String plainTextToken) {
        String tokenHash = TokenUtils.hashToken(plainTextToken);
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        refreshTokenOpt.ifPresent(token -> {
            User user = token.getUser();
            refreshTokenRepository.delete(token);
        });

        return refreshTokenOpt.map(RefreshToken::getUser);
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public long countActiveTokens(User user) {
        return refreshTokenRepository.countByUser(user);
    }
}

