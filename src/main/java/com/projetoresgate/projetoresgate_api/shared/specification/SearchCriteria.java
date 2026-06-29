package com.projetoresgate.projetoresgate_api.shared.specification;

public record SearchCriteria(
        String key,
        String operation,
        Object value
) {
}