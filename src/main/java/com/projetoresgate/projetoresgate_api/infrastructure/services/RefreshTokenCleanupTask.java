package com.projetoresgate.projetoresgate_api.infrastructure.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "api.security.refresh-token.cleanup.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RefreshTokenCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupTask.class);

    private final IRefreshTokenService refreshTokenService;

    public RefreshTokenCleanupTask(IRefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(fixedRateString = "${api.security.refresh-token.cleanup.rate:3600000}")
    public void cleanupExpiredRefreshTokens() {
        try {
            log.info("Iniciando limpeza de tokens de renovação expirados");
            refreshTokenService.cleanupExpiredTokens();
            log.info("Limpeza de tokens de renovação concluída com sucesso");
        } catch (Exception e) {
            log.error("Erro ao limpar tokens de renovação expirados", e);
        }
    }
}

