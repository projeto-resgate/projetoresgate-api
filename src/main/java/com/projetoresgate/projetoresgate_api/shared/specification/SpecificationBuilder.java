package com.projetoresgate.projetoresgate_api.shared.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public class SpecificationBuilder<T> {

    private final List<SearchCriteria> params = new ArrayList<>();

    public SpecificationBuilder<T> with(String key, String operation, Object value) {
        if (nonNull(value) && !value.toString().trim().isEmpty()) {
            params.add(new SearchCriteria(key, operation, value));
        }
        return this;
    }

    public Specification<T> build() {
        if (params.isEmpty()) {
            return null;
        }

        Specification<T> result = new GenericSpecification<>(params.get(0));

        for (int i = 1; i < params.size(); i++) {
            result = Specification.where(result).and(new GenericSpecification<>(params.get(i)));
        }

        return result;
    }
}