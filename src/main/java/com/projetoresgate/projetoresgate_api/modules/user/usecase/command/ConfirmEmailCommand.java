package com.projetoresgate.projetoresgate_api.modules.user.usecase.command;

import com.projetoresgate.projetoresgate_api.modules.user.domain.User;

public record ConfirmEmailCommand(
        String token,
        User user
) {
}
