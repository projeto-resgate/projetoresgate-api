package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("LegalPersonSummaryResponse - DTO Test")
class LegalPersonSummaryResponseTest {

    @Test
    @DisplayName("Deve mapear id, displayName e cnpj da pessoa jurídica")
    void fromEntity_ShouldMapFields() {
        LegalPerson person = LegalPerson.create(
                "11222333000181", "Razão Social LTDA", "Nome Fantasia", "Acme LTDA", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP"), null);

        LegalPersonSummaryResponse response = LegalPersonSummaryResponse.fromEntity(person);

        assertNotNull(response);
        assertEquals(person.getId(), response.id());
        assertEquals("Acme LTDA", response.displayName());
        assertEquals("11222333000181", response.cnpj());
    }
}