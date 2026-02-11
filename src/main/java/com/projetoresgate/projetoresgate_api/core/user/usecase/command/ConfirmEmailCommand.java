package com.projetoresgate.projetoresgate_api.core.user.usecase.command;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;

public record ConfirmEmailCommand(
        String token,
        User user
) {
}
