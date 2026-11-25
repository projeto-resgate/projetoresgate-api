package com.projetoresgate.projetoresgate_api.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("MaskUtils - Test")
class MaskUtilsTest {

    @Test
    @DisplayName("Deve remover pontos, traços, barras, parênteses e espaços")
    void unmask_ShouldRemoveMaskCharacters() {
        assertEquals("12345678909", MaskUtils.unmask("123.456.789-09"));
        assertEquals("11222333000181", MaskUtils.unmask("11.222.333/0001-81"));
        assertEquals("47999990000", MaskUtils.unmask("(47) 99999-0000"));
        assertEquals("89010000", MaskUtils.unmask("89010-000"));
        assertEquals("6201500", MaskUtils.unmask("6201-5/00"));
    }

    @Test
    @DisplayName("Não deve remover letras, para que a validação recuse valores inválidos")
    void unmask_ShouldKeepLetters() {
        assertEquals("abc", MaskUtils.unmask("abc"));
        assertEquals("1234X", MaskUtils.unmask("1.234-X"));
    }

    @Test
    @DisplayName("Vazio ou só máscara deve virar nulo")
    void unmask_ShouldReturnNullForBlankValues() {
        assertNull(MaskUtils.unmask(null));
        assertNull(MaskUtils.unmask(""));
        assertNull(MaskUtils.unmask("  "));
        assertNull(MaskUtils.unmask("..-"));
    }

    @Test
    @DisplayName("Deve deixar em maiúsculas quando pedido")
    void unmaskUpperCase_ShouldUpperCase() {
        assertEquals("MG12345678", MaskUtils.unmaskUpperCase("mg-12.345.678"));
        assertNull(MaskUtils.unmaskUpperCase(""));
    }
}
