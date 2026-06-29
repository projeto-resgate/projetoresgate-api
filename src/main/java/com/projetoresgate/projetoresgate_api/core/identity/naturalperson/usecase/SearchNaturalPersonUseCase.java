package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.SearchNaturalPersonQuery;
import org.springframework.data.domain.Page;

public interface SearchNaturalPersonUseCase {
    Page<NaturalPerson> handle(SearchNaturalPersonQuery query);
}
