package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindAllFamilyGroupsQuery;
import org.springframework.data.domain.Page;

public interface FindAllFamilyGroupsUseCase {
    Page<FamilyGroupSummaryResponse> handle(FindAllFamilyGroupsQuery query);
}
