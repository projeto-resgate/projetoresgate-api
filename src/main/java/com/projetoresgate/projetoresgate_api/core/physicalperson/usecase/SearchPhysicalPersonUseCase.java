package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.SearchPhysicalPersonQuery;
import org.springframework.data.domain.Page;

public interface SearchPhysicalPersonUseCase {
    Page<PhysicalPerson> handle(SearchPhysicalPersonQuery query);
}
