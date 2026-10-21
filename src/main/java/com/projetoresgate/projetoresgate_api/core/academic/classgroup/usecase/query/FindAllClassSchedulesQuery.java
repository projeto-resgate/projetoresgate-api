package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record FindAllClassSchedulesQuery(
        UUID classGroupId,
        WeekDay weekDay,
        Boolean active,
        Pageable pageable
) {
}
