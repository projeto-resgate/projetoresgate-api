package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.AutocompleteCollaboratorQuery;

import java.util.List;

public interface AutocompleteCollaboratorUseCase {
    List<CollaboratorAutocompleteResponse> handle(AutocompleteCollaboratorQuery query);
}
