package com.projetoresgate.projetoresgate_api.core.user.usecase;

public interface ResetPasswordUseCase {
    void handle(String token, String newPassword);
}
