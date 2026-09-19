package com.projetoresgate.projetoresgate_api.core.academic.program.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;

public record ProgramNameResponse(
        String name
) {
    public static ProgramNameResponse fromEntity(Program entity) {
        return new ProgramNameResponse(entity.getName());
    }
}