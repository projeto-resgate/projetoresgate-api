package com.projetoresgate.projetoresgate_api.modules.user.usecase;

public interface ResetPasswordUseCase {
    void handle(String token, String newPassword);
}
