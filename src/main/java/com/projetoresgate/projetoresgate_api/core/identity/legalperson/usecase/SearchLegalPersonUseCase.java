package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.SearchLegalPersonQuery;
import org.springframework.data.domain.Page;

public interface SearchLegalPersonUseCase {
    Page<LegalPerson> handle(SearchLegalPersonQuery query);
}
