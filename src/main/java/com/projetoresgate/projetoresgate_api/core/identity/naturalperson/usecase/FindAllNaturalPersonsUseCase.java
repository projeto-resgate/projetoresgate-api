package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindAllNaturalPersonsQuery;
import org.springframework.data.domain.Page;

public interface FindAllNaturalPersonsUseCase {
    Page<NaturalPerson> handle(FindAllNaturalPersonsQuery query);
}
