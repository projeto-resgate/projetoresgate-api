package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.UUID;

public record UpdateLegalPersonCommand(
        @JsonIgnore
        UUID id,
        String corporateName,
        String tradeName,
        String displayName,
        @CNPJ(message = "CNPJ inválido!")
        String cnpj,
        String mainCnaeCode,
        RegistrationStatus registrationStatus,
        CompanyStatus companyStatus,
        @Valid
        Address address,
        @Valid
        Representative representative
) {
    public UpdateLegalPersonCommand(UUID id, String corporateName, String tradeName, String displayName, String cnpj, String mainCnaeCode, RegistrationStatus registrationStatus, CompanyStatus companyStatus, Address address) {
        this(id, corporateName, tradeName, displayName, cnpj, mainCnaeCode, registrationStatus, companyStatus, address, null);
    }

    public UpdateLegalPersonCommand withId(UUID id) {
        return new UpdateLegalPersonCommand(
                id,
                this.corporateName,
                this.tradeName,
                this.displayName,
                this.cnpj,
                this.mainCnaeCode,
                this.registrationStatus,
                this.companyStatus,
                this.address,
                this.representative
        );
    }
}
