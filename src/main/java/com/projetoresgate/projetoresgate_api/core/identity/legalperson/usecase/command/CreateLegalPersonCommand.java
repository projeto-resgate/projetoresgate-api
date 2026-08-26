package com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CNPJ;

public record CreateLegalPersonCommand(
        @NotBlank(message = "A razão social é obrigatória")
        String corporateName,
        String tradeName,
        String displayName,
        @CNPJ(message = "CNPJ inválido!")
        String cnpj,
        String mainCnaeCode,
        @NotNull(message = "O status de registro é obrigatório")
        RegistrationStatus registrationStatus,
        @NotNull(message = "O status da empresa é obrigatório")
        CompanyStatus companyStatus,
        @Valid
        Address address,
        @Valid
        Representative representative
) {
    public CreateLegalPersonCommand(String corporateName, String tradeName, String displayName, String cnpj, String mainCnaeCode, RegistrationStatus registrationStatus, CompanyStatus companyStatus, Address address) {
        this(corporateName, tradeName, displayName, cnpj, mainCnaeCode, registrationStatus, companyStatus, address, null);
    }
}
