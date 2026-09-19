package com.projetoresgate.projetoresgate_api.core.identity.address.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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

    @Test
    @DisplayName("Deve atualizar os campos usando o Inner Updater mantendo o mesmo id")
    void updater_ShouldUpdateFields() {
        Address address = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
        UUID originalId = address.getId();

        address.update()
                .zipCode("20040-020")
                .number("200")
                .complement("Sala 5")
                .neighborhood("Centro")
                .city("Rio de Janeiro")
                .state("RJ")
                .apply();

        assertEquals(originalId, address.getId());
        assertEquals("20040-020", address.getZipCode());
        assertEquals("200", address.getNumber());
        assertEquals("Sala 5", address.getComplement());
        assertEquals("Centro", address.getNeighborhood());
        assertEquals("Rio de Janeiro", address.getCity());
        assertEquals("RJ", address.getState());
    }

    @Test
    @DisplayName("Deve lançar exceção no apply do Updater se a validação falhar")
    void updater_ShouldRevalidateOnApply() {
        Address address = Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");

        InternalException exception = assertThrows(InternalException.class, () ->
                address.update().zipCode("").apply()
        );
        assertEquals("O CEP não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o número exceder 20 caracteres")
    void validate_ShouldFailIfNumberTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "123456789012345678901", null, null, "São Paulo", "SP")
        );
        assertEquals("O número não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o complemento exceder 100 caracteres")
    void validate_ShouldFailIfComplementTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "1000", "A".repeat(101), null, "São Paulo", "SP")
        );
        assertEquals("O complemento não pode exceder 100 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se a cidade exceder 100 caracteres")
    void validate_ShouldFailIfCityTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "1000", null, null, "A".repeat(101), "SP")
        );
        assertEquals("A cidade não pode exceder 100 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o estado exceder 50 caracteres")
    void validate_ShouldFailIfStateTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "1000", null, null, "São Paulo", "A".repeat(51))
        );
        assertEquals("O estado não pode exceder 50 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o bairro exceder 100 caracteres")
    void validate_ShouldFailIfNeighborhoodTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("01310-100", "1000", null, "A".repeat(101), "São Paulo", "SP")
        );
        assertEquals("O bairro não pode exceder 100 caracteres.", exception.getMessage());
    }
}
