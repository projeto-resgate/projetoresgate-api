package com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public record SearchFamilyGroupQuery(
        String searchTerm,
        String name,
        BigDecimal minHouseholdIncome,
        BigDecimal maxHouseholdIncome,
        BigDecimal minPerCapitaIncome,
        BigDecimal maxPerCapitaIncome,
        Integer numberOfResidents,
        Pageable pageable
) {
}
