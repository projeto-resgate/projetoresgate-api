package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;

public interface UpdateClassGroupUseCase {
    ClassGroupResponse handle(UpdateClassGroupCommand command);
}
