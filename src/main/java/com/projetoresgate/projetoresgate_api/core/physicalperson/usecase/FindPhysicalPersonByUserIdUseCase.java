package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByUserIdQuery;

public interface FindPhysicalPersonByUserIdUseCase {
    PhysicalPerson handle(FindPhysicalPersonByUserIdQuery query);
}
