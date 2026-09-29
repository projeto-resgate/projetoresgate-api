package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupByIdQuery;

public interface FindFamilyGroupByIdUseCase {
    FamilyGroup handle(FindFamilyGroupByIdQuery query);
}
