package com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query;

public record AutocompleteCollaboratorQuery(
        String searchTerm,
        int limit
) {
}
