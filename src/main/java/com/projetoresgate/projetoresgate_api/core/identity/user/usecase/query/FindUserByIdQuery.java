package com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query;

import java.util.UUID;

public record FindUserByIdQuery(
        UUID id
) {
}
