package com.projetoresgate.projetoresgate_api.core.academic.program.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;

import java.util.UUID;

public record ProgramNameResponse(
        UUID id,
        String name
) {
    public static ProgramNameResponse fromEntity(Program entity) {
        return new ProgramNameResponse(
                entity.getId(),
                entity.getName()
        );
    }
}