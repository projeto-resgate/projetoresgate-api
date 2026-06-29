package com.projetoresgate.projetoresgate_api.shared.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

public class GenericSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    public GenericSpecification(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        Path<String> path = getPath(root, criteria.key());

        if (criteria.operation().equalsIgnoreCase(">")) {
            return builder.greaterThanOrEqualTo(path, criteria.value().toString());
        } else if (criteria.operation().equalsIgnoreCase("<")) {
            return builder.lessThanOrEqualTo(path, criteria.value().toString());

        } else if (criteria.operation().equalsIgnoreCase(":")) {
            if (path.getJavaType() == String.class) {
                return builder.like(builder.lower(path), "%" + criteria.value().toString().toLowerCase() + "%");
            } else {
                return builder.equal(path, criteria.value());
            }
        }
        return null;
    }

    private Path<String> getPath(Root<T> root, String key) {
        if (!key.contains(".")) {
            return root.get(key);
        }
        String[] keys = key.split("\\.");
        Path<String> path = root.get(keys[0]);
        for (int i = 1; i < keys.length; i++) {
            path = path.get(keys[i]);
        }
        return path;
    }
}