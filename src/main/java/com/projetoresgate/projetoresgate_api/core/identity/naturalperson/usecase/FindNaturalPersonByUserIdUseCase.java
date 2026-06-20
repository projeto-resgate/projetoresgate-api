package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByUserIdQuery;

public interface FindNaturalPersonByUserIdUseCase {
    NaturalPerson handle(FindNaturalPersonByUserIdQuery query);
}
