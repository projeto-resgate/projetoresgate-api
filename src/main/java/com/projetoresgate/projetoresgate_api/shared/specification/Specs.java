package com.projetoresgate.projetoresgate_api.shared.specification;

import org.springframework.data.jpa.domain.Specification;

public final class Specs {

    private Specs() {
    }

    @SafeVarargs
    public static <T> Specification<T> allOf(Specification<T>... specs) {
        Specification<T> result = Specification.unrestricted();
        for (Specification<T> spec : specs) {
            if (spec != null) {
                result = result.and(spec);
            }
        }
        return result;
    }
}
