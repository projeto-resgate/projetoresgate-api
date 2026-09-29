package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import org.springframework.data.domain.Page;

public interface FindFamilyGroupNaturalPersonsUseCase {
    Page<FamilyGroupNaturalPersonResponse> handle(FindFamilyGroupNaturalPersonsQuery query);
}
