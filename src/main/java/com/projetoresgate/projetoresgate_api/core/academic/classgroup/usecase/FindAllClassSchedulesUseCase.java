package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassSchedulesQuery;
import org.springframework.data.domain.Page;

public interface FindAllClassSchedulesUseCase {
    Page<ClassScheduleResponse> handle(FindAllClassSchedulesQuery query);
}
