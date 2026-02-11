package com.projetoresgate.projetoresgate_api.modules.user.usecase;

import com.projetoresgate.projetoresgate_api.modules.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.query.AuthenticateUserQuery;

public interface AuthenticateUserUseCase {

    AuthenticationResponse handle(AuthenticateUserQuery query);

}
