package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindClassGroupByIdQuery;

public interface FindClassGroupByIdUseCase {
    ClassGroupResponse handle(FindClassGroupByIdQuery query);
}
