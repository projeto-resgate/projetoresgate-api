package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LegalPersonResponse - DTO Test")
class LegalPersonResponseTest {

    @Test
    @DisplayName("Deve mapear a pessoa jurídica com todos os campos, endereço, representante e data de cadastro")
    void fromEntity_ShouldMapAllFields() {
        LocalDateTime dateCreated = LocalDateTime.of(2025, 11, 23, 11, 5, 20);
        Address address = Address.create("01310-100", "1000", "Apto 101", "Bela Vista", "São Paulo", "SP");
        Representative representative = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");

        LegalPerson person = LegalPerson.create(
                "11222333000181", "Razão Social LTDA", "Nome Fantasia", "Display Name", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, representative);
        person.setDateCreated(dateCreated);

        LegalPersonResponse response = LegalPersonResponse.fromEntity(person);

        assertNotNull(response);
        assertEquals(person.getId(), response.id());
        assertEquals("11222333000181", response.cnpj());
        assertEquals("Razão Social LTDA", response.corporateName());
        assertEquals("Nome Fantasia", response.tradeName());
        assertEquals("Display Name", response.displayName());
        assertEquals("6201-5/00", response.mainCnaeCode());
        assertEquals(RegistrationStatus.ACTIVE, response.registrationStatus());
        assertEquals(CompanyStatus.ACTIVE, response.companyStatus());
        assertEquals(dateCreated, response.dateCreated());

        assertNotNull(response.address());
        assertEquals(address.getId(), response.address().id());
        assertEquals("São Paulo", response.address().city());
        assertEquals("01310-100", response.address().zipCode());

        assertNotNull(response.representative());
        assertEquals("John Doe", response.representative().name());
        assertEquals("john@doe.com", response.representative().email());
    }

    @Test
    @DisplayName("Deve retornar endereço e representante null quando a entidade não tiver")
    void fromEntity_ShouldReturnNullAddressAndRepresentative() {
        LocalDateTime dateCreated = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        LegalPerson person = LegalPerson.create(
                "11222333000181", "Razão Social LTDA", null, null, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP"), null);
        person.setDateCreated(dateCreated);

        LegalPersonResponse response = LegalPersonResponse.fromEntity(person);

        assertNotNull(response.address());
        assertNull(response.representative());
        assertEquals(dateCreated, response.dateCreated());
    }
}