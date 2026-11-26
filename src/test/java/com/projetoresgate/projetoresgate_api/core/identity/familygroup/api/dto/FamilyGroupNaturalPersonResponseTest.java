package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("FamilyGroupNaturalPersonResponse - Test")
class FamilyGroupNaturalPersonResponseTest {

    @Test
    @DisplayName("Deve mapear apenas os campos da grid da tela de grupo familiar")
    void fromEntity_ShouldMapOnlyGridFields() {
        NaturalPerson person = NaturalPerson.create(
                "Daniela Ferreira", "daniela@email.com", "Dani", "11144477735", "123456789",
                null, null, null, "11911112222");

        UUID learnerId = UUID.randomUUID();

        FamilyGroupNaturalPersonResponse response = FamilyGroupNaturalPersonResponse.fromEntity(person, List.of(), learnerId);

        assertEquals(person.getId(), response.id());
        assertEquals(learnerId, response.learnerId());
        assertEquals("Daniela Ferreira", response.name());
        assertEquals("123456789", response.rg());
        assertEquals("11144477735", response.cpf());
        assertEquals("11911112222", response.cellphone());
    }

    @Test
    @DisplayName("Deve retornar null nos campos opcionais que a pessoa não possui")
    void fromEntity_ShouldReturnNullForMissingOptionalFields() {
        NaturalPerson person = NaturalPerson.create(
                "Breno Ferreira", "breno@email.com", null, null, null, null, null, null, null);

        FamilyGroupNaturalPersonResponse response = FamilyGroupNaturalPersonResponse.fromEntity(person, List.of(), null);

        assertEquals("Breno Ferreira", response.name());
        assertNull(response.learnerId());
        assertNull(response.rg());
        assertNull(response.cpf());
        assertNull(response.cellphone());
    }
}
