package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("NaturalPersonSummaryResponse - DTO Test")
class NaturalPersonSummaryResponseTest {

    @Test
    @DisplayName("Deve mapear id, name, rg e cpf da pessoa física")
    void fromEntity_ShouldMapFields() {
        NaturalPerson person = NaturalPerson.create(
                "João Silva", "joao@test.com", "joaosilva", "51086174968", "1234567",
                LocalDate.of(1990, 1, 1), Gender.MALE, "1133334444", "11999999999");

        NaturalPersonSummaryResponse response = NaturalPersonSummaryResponse.fromEntity(person);

        assertNotNull(response);
        assertEquals(person.getId(), response.id());
        assertEquals("João Silva", response.name());
        assertEquals("1234567", response.rg());
        assertEquals("51086174968", response.cpf());
    }
}