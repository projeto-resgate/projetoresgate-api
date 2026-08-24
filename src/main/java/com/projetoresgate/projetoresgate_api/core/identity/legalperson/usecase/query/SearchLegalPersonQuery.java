package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import org.springframework.data.domain.Pageable;

public record SearchLegalPersonQuery(
        String searchTerm,
        String cnpj,
        String corporateName,
        RegistrationStatus registrationStatus,
        CompanyStatus companyStatus,
        Pageable pageable
) {
}
