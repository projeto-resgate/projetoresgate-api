package com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rg - Value Object Test")
class RgTest {

    @Test
    @DisplayName("Deve criar um RG válido")
    void constructor_ShouldSucceedWithValidRg() {
        String validRg = "7068613";
        Rg rg = new Rg(validRg);
        assertEquals(validRg, rg.getValue());
    }

    @Test
    @DisplayName("Deve limpar e formatar em maiúsculas ao criar")
    void constructor_ShouldCleanAndFormat() {
        Rg rg = new Rg("7.068-613a");
        assertEquals("7068613A", rg.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "1234567890123456", ""})
    @DisplayName("Deve lançar exceção para RGs inválidos por tamanho")
    void constructor_ShouldFailWithInvalidRgLength(String invalidRg) {
        if (invalidRg.isEmpty()) {
             Rg rg = new Rg(invalidRg);
             assertNull(rg.getValue());
        } else {
            assertThrows(InternalException.class, () -> new Rg(invalidRg));
        }
    }

    @Test
    @DisplayName("Deve permitir RG nulo")
    void constructor_ShouldAllowNull() {
        Rg rg = new Rg(null);
        assertNull(rg.getValue());
    }
}
