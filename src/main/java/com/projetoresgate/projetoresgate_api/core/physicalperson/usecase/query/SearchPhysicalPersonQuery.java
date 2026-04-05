package com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Rg;
import org.springframework.data.domain.Pageable;

public record SearchPhysicalPersonQuery(
        String searchTerm,
        Rg rg,
        Cpf cpf,
        String cellphone,
        Gender gender,
        Pageable pageable
) {}
