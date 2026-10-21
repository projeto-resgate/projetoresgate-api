package com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.RefreshClassGroupTitlesCommand;

public interface RefreshClassGroupTitlesUseCase {

    void handle(RefreshClassGroupTitlesCommand command);
}
