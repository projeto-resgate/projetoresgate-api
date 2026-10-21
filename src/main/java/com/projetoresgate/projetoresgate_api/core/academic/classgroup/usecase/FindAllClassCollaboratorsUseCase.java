package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassCollaboratorsQuery;
import org.springframework.data.domain.Page;

public interface FindAllClassCollaboratorsUseCase {
    Page<ClassCollaboratorResponse> handle(FindAllClassCollaboratorsQuery query);
}
