package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.user.usecase.query.AuthenticateUserQuery;

public interface AuthenticateUserUseCase {

    AuthenticationResponse handle(AuthenticateUserQuery query);

}
