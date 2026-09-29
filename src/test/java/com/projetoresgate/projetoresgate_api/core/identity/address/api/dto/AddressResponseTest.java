package com.projetoresgate.projetoresgate_api.core.identity.address.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AddressResponse - DTO Test")
class AddressResponseTest {

    @Test
    @DisplayName("Deve mapear todos os campos do endereço")
    void fromEntity_ShouldMapAllFields() {
        Address address = Address.create("Rua", "das Palmeiras", "01310-100", "1000", "Próximo à praça",
                "Apto 101", "Bela Vista", "São Paulo", "SP");

        AddressResponse response = AddressResponse.fromEntity(address);

        assertNotNull(response);
        assertEquals(address.getId(), response.id());
        assertEquals("Rua", response.streetType());
        assertEquals("das Palmeiras", response.streetName());
        assertEquals("01310-100", response.zipCode());
        assertEquals("1000", response.number());
        assertEquals("Próximo à praça", response.referencePoint());
        assertEquals("Apto 101", response.complement());
        assertEquals("Bela Vista", response.neighborhood());
        assertEquals("São Paulo", response.city());
        assertEquals("SP", response.state());
    }

    @Test
    @DisplayName("Deve retornar null quando o endereço não existir")
    void fromEntity_ShouldReturnNullWhenAddressIsNull() {
        assertNull(AddressResponse.fromEntity(null));
    }
}
