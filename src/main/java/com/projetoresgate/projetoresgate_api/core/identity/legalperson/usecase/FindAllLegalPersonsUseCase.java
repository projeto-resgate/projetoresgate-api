package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.FindAllLegalPersonsQuery;
import org.springframework.data.domain.Page;

public interface FindAllLegalPersonsUseCase {
    Page<LegalPerson> handle(FindAllLegalPersonsQuery query);
}
