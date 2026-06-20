package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.AuthenticateUserQuery;

public interface AuthenticateUserUseCase {

    AuthenticationResponse handle(AuthenticateUserQuery query);

}
