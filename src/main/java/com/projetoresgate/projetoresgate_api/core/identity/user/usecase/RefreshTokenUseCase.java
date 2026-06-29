package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.RefreshTokenResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.RefreshTokenQuery;

public interface RefreshTokenUseCase {

    RefreshTokenResponse handle(RefreshTokenQuery query);

}

