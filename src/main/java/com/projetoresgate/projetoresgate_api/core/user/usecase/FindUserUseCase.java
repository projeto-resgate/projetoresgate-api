package com.projetoresgate.projetoresgate_api.core.user.usecase;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.usecase.query.FindUserByIdQuery;

public interface FindUserUseCase {

    User handle(FindUserByIdQuery query);

}
