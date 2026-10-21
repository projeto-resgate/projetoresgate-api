package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassScheduleCommand;

public interface UpdateClassScheduleUseCase {
    ClassScheduleResponse handle(UpdateClassScheduleCommand command);
}
