package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepresentativeResponse - DTO Test")
class RepresentativeResponseTest {

    @Test
    @DisplayName("Deve mapear um representante para o response com todos os campos")
    void fromEntity_ShouldMapAllFields() {
        Representative representative = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");

        RepresentativeResponse response = RepresentativeResponse.fromEntity(representative);

        assertNotNull(response);
        assertEquals("John Doe", response.name());
        assertEquals("11988887777", response.cellphone());
        assertEquals("1133334444", response.phone());
        assertEquals("john@doe.com", response.email());
    }

    @Test
    @DisplayName("Deve retornar null quando o representante for nulo")
    void fromEntity_ShouldReturnNullForNullRepresentative() {
        assertNull(RepresentativeResponse.fromEntity(null));
    }
}