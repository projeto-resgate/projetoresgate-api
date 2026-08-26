package com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Representative - Value Object Test")
class RepresentativeTest {

    @Test
    @DisplayName("Deve criar um representante com sucesso")
    void create_ShouldSucceed() {
        Representative rep = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");

        assertNotNull(rep);
        assertEquals("John Doe", rep.getName());
        assertEquals("11988887777", rep.getCellphone());
        assertEquals("1133334444", rep.getPhone());
        assertEquals("john@doe.com", rep.getEmail());
    }

    @Test
    @DisplayName("Deve criar um representante com apenas nome")
    void create_ShouldSucceedWithOnlyName() {
        Representative rep = Representative.create("Jane Doe", null, null, null);

        assertNotNull(rep);
        assertEquals("Jane Doe", rep.getName());
        assertNull(rep.getCellphone());
        assertNull(rep.getPhone());
        assertNull(rep.getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem nome")
    void create_ShouldFailWithoutName() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Representative.create("", "11988887777", "1133334444", "john@doe.com")
        );
        assertEquals("O nome do representante não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com nome nulo")
    void create_ShouldFailWithNullName() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Representative.create(null, null, null, null)
        );
        assertEquals("O nome do representante não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o nome exceder 255 caracteres")
    void validate_ShouldFailIfNameTooLong() {
        String longName = "A".repeat(256);
        InternalException exception = assertThrows(InternalException.class, () ->
                Representative.create(longName, null, null, null)
        );
        assertEquals("O nome do representante não pode exceder 255 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o celular exceder 20 caracteres")
    void validate_ShouldFailIfCellphoneTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Representative.create("John Doe", "123456789012345678901", null, null)
        );
        assertEquals("O celular do representante não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o telefone exceder 20 caracteres")
    void validate_ShouldFailIfPhoneTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Representative.create("John Doe", null, "123456789012345678901", null)
        );
        assertEquals("O telefone do representante não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o e-mail exceder 255 caracteres")
    void validate_ShouldFailIfEmailTooLong() {
        String longEmail = "a".repeat(247) + "@test.com";
        InternalException exception = assertThrows(InternalException.class, () ->
                Representative.create("John Doe", null, null, longEmail)
        );
        assertEquals("O e-mail do representante não pode exceder 255 caracteres.", exception.getMessage());
    }
}
