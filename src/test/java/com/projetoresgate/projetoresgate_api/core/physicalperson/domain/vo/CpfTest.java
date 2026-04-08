package com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cpf - Value Object Test")
class CpfTest {

    @Test
    @DisplayName("Deve criar um CPF válido")
    void constructor_ShouldSucceedWithValidCpf() {
        String validCpf = "51086174968";
        Cpf cpf = new Cpf(validCpf);
        assertEquals(validCpf, cpf.getValue());
    }

    @Test
    @DisplayName("Deve limpar caracteres não numéricos ao criar")
    void constructor_ShouldCleanNonNumericCharacters() {
        Cpf cpf = new Cpf("510.861.749-68");
        assertEquals("51086174968", cpf.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "00000000000", "11111111111", "123", ""})
    @DisplayName("Deve lançar exceção para CPFs inválidos")
    void constructor_ShouldFailWithInvalidCpf(String invalidCpf) {
        if (invalidCpf.isEmpty()) {
             Cpf cpf = new Cpf(invalidCpf);
             assertNull(cpf.getValue());
        } else {
            assertThrows(InternalException.class, () -> new Cpf(invalidCpf));
        }
    }

    @Test
    @DisplayName("Deve permitir CPF nulo (valor interno nulo)")
    void constructor_ShouldAllowNull() {
        Cpf cpf = new Cpf(null);
        assertNull(cpf.getValue());
    }
}
