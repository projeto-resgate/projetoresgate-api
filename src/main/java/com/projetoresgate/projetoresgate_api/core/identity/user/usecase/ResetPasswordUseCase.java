package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

public interface ResetPasswordUseCase {
    void handle(String token, String newPassword);
}
