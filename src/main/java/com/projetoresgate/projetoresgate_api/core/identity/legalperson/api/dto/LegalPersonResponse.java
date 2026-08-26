package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;

import java.util.UUID;

public record LegalPersonResponse(
        UUID id,
        String cnpj,
        String corporateName,
        String tradeName,
        String displayName,
        String mainCnaeCode,
        RegistrationStatus registrationStatus,
        CompanyStatus companyStatus,
        Address address,
        Representative representative
) {
    public static LegalPersonResponse fromEntity(LegalPerson entity) {
        return new LegalPersonResponse(
                entity.getId(),
                entity.getCnpj(),
                entity.getCorporateName(),
                entity.getTradeName(),
                entity.getDisplayName(),
                entity.getMainCnaeCode(),
                entity.getRegistrationStatus(),
                entity.getCompanyStatus(),
                entity.getAddress(),
                entity.getRepresentative()
        );
    }
}
