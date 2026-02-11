package com.projetoresgate.projetoresgate_api.modules.user.usecase;

import com.projetoresgate.projetoresgate_api.modules.user.domain.User;
import com.projetoresgate.projetoresgate_api.modules.user.usecase.query.FindUserByIdQuery;

public interface FindUserUseCase {

    User handle(FindUserByIdQuery query);

}
