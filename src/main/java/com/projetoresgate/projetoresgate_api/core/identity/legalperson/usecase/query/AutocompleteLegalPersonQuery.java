package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query;

public record AutocompleteLegalPersonQuery(
        String searchTerm,
        int limit
) {
}
