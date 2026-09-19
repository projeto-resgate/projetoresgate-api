package com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record SearchProgramQuery(
        ProgramStatus status,
        UUID institutionId,
        Pageable pageable
) {
}