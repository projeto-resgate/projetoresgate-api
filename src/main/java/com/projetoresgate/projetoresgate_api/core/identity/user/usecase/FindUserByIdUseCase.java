package com.projetoresgate.projetoresgate_api.core.identity.user.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.FindUserByIdQuery;

public interface FindUserByIdUseCase {

    User handle(FindUserByIdQuery query);
}
