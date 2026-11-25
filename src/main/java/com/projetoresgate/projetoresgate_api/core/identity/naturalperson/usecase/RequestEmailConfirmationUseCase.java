package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

public interface RequestEmailConfirmationUseCase {
    void handle(String email);

    void handle(NaturalPerson person);
}
