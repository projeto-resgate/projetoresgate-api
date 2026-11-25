package com.projetoresgate.projetoresgate_api.core.academic.program.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Objects.isNull;

public final class EducatorCategoryNames {

    private EducatorCategoryNames() {
    }

    public static void requireUnique(List<String> names) {
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            if (isNull(name)) {
                continue;
            }
            if (!seen.add(name.trim().toLowerCase(Locale.ROOT))) {
                throw new InternalException("O programa não pode ter duas categorias de educador com o mesmo nome: " + name.trim() + ".");
            }
        }
    }
}
