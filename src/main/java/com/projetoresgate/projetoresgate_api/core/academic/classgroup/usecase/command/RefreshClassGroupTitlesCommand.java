package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import java.util.UUID;

public record RefreshClassGroupTitlesCommand(
        UUID programId,
        UUID schoolGradeId
) {
    public static RefreshClassGroupTitlesCommand byProgram(UUID programId) {
        return new RefreshClassGroupTitlesCommand(programId, null);
    }

    public static RefreshClassGroupTitlesCommand bySchoolGrade(UUID schoolGradeId) {
        return new RefreshClassGroupTitlesCommand(null, schoolGradeId);
    }
}
