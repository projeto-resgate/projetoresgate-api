package com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AddressResponse - DTO Test")
class AddressResponseTest {

    @Test
    @DisplayName("Deve mapear um endereço para o response com todos os campos")
    void fromEntity_ShouldMapAllFields() {
        Address address = Address.create("01310-100", "1000", "Apto 101", "Bela Vista", "São Paulo", "SP");

        AddressResponse response = AddressResponse.fromEntity(address);

        assertNotNull(response);
        assertEquals(address.getId(), response.id());
        assertEquals("01310-100", response.zipCode());
        assertEquals("1000", response.number());
        assertEquals("Apto 101", response.complement());
        assertEquals("Bela Vista", response.neighborhood());
        assertEquals("São Paulo", response.city());
        assertEquals("SP", response.state());
    }

    @Test
    @DisplayName("Deve retornar null quando o endereço for nulo")
    void fromEntity_ShouldReturnNullForNullAddress() {
        assertNull(AddressResponse.fromEntity(null));
    }
}