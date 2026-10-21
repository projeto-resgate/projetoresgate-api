package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassLearnersQuery;
import org.springframework.data.domain.Page;

public interface FindAllClassLearnersUseCase {
    Page<ClassLearnerResponse> handle(FindAllClassLearnersQuery query);
}
