package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import org.springframework.data.domain.Pageable;

public record SearchNaturalPersonQuery(
        String searchTerm,
        String rg,
        String cpf,
        String cellphone,
        Gender gender,
        Pageable pageable
) {
}
