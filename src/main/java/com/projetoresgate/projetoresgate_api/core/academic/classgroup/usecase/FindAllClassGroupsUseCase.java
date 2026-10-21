package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import org.springframework.data.domain.Page;

public interface FindAllClassGroupsUseCase {
    Page<ClassGroupSummaryResponse> handle(FindAllClassGroupsQuery query);
}
