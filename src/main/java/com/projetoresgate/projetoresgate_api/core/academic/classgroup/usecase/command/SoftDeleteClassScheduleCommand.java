package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command;

import java.util.UUID;

public record SoftDeleteClassScheduleCommand(
        UUID classGroupId,
        UUID id
) {
}
