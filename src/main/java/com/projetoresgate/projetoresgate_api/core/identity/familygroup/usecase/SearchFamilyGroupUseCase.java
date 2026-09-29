package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.SearchFamilyGroupQuery;
import org.springframework.data.domain.Page;

public interface SearchFamilyGroupUseCase {
    Page<FamilyGroupSummaryResponse> handle(SearchFamilyGroupQuery query);
}
