package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassScheduleCommand;

public interface SoftDeleteClassScheduleUseCase {
    void handle(SoftDeleteClassScheduleCommand command);
}
