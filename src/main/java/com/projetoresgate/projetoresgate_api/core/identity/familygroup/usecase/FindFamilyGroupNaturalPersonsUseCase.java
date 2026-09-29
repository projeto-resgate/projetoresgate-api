package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;

import java.util.List;

public interface FindFamilyGroupNaturalPersonsUseCase {
    List<NaturalPerson> handle(FindFamilyGroupNaturalPersonsQuery query);
}
