package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query;

public record AutocompleteNaturalPersonQuery(
        String searchTerm,
        int limit
) {
    public AutocompleteNaturalPersonQuery(String searchTerm) {
        this(searchTerm, 10);
    }
}