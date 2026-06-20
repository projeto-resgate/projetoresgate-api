package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

public interface RequestPasswordResetUseCase {
    void handle(String email);
}
