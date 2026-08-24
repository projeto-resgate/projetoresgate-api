package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.FindLegalPersonByIdQuery;

public interface FindLegalPersonByIdUseCase {
    LegalPerson handle(FindLegalPersonByIdQuery query);
}
