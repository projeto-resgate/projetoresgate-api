package com.projetoresgate.projetoresgate_api.shared.validation.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RGValidator - Test")
class RGValidatorTest {

    private final RGValidator validator = new RGValidator();

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "12345678-9", "12.345.678-X", "MG-12.345.678", "mg1234567", "12345678901234"})
    @DisplayName("Deve aceitar RGs com pontuação, prefixo de UF e dígito X")
    void isValid_ShouldAcceptCommonFormats(String rg) {
        assertTrue(validator.isValid(rg, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12", "1234-ABC", "X1234567", "1234567890123456"})
    @DisplayName("Deve recusar RGs fora do formato")
    void isValid_ShouldRejectInvalidValues(String rg) {
        assertFalse(validator.isValid(rg, null));
    }
}
