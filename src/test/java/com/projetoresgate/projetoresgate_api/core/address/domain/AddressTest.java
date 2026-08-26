package com.projetoresgate.projetoresgate_api.core.address.domain;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Address - Entity Test")
class AddressTest {

    @Test
    @DisplayName("Deve criar um endereço com sucesso")
    void create_ShouldSucceed() {
        Address address = Address.create("01310-100", "1000", "Apto 101", "Bela Vista", "São Paulo", "SP");

        assertNotNull(address);
        assertNotNull(address.getId());
        assertEquals("01310-100", address.getZipCode());
        assertEquals("1000", address.getNumber());
        assertEquals("Apto 101", address.getComplement());
        assertEquals("Bela Vista", address.getNeighborhood());
        assertEquals("São Paulo", address.getCity());
        assertEquals("SP", address.getState());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem CEP")
    void create_ShouldFailWithoutZipCode() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("", "1000", null, null, "São Paulo", "SP")
        );
        assertEquals("O CEP não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem cidade")
    void create_ShouldFailWithoutCity() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "1000", null, null, "", "SP")
        );
        assertEquals("A cidade não pode ser vazia.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem estado")
    void create_ShouldFailWithoutState() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "1000", null, null, "São Paulo", null)
        );
        assertEquals("O estado não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o CEP exceder 20 caracteres")
    void validate_ShouldFailIfZipCodeTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("0123456789012345678901", "1000", null, null, "São Paulo", "SP")
        );
        assertEquals("O CEP não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Endereços com os mesmos valores devem ser iguais")
    void equals_ShouldBeTrueForSameValues() {
        Address address1 = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
        Address address2 = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");

        assertEquals(address1, address2);
        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    @DisplayName("Endereços com valores diferentes devem ser diferentes")
    void equals_ShouldBeFalseForDifferentValues() {
        Address address1 = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
        Address address2 = Address.create("01310-200", "2000", null, "Consolação", "São Paulo", "SP");

        assertNotEquals(address1, address2);
    }
}
