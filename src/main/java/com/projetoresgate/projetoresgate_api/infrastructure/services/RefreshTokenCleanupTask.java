package com.projetoresgate.projetoresgate_api.infrastructure.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@ConditionalOnProperty(
        name = "api.security.refresh-token.cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RefreshTokenCleanupTask {

    private static final Logger logger = Logger.getLogger(RefreshTokenCleanupTask.class.getName());

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupTask(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(fixedRateString = "${api.security.refresh-token.cleanup.rate:3600000}")
    public void cleanupExpiredRefreshTokens() {
        try {
            logger.info("Iniciando limpeza de tokens de renovação expirados");
            refreshTokenService.cleanupExpiredTokens();
            logger.info("Limpeza de tokens de renovação concluída com sucesso");
        } catch (Exception e) {
            logger.severe("Erro ao limpar tokens de renovação expirados: " + e.getMessage());
        }
    }
}

