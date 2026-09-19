package com.projetoresgate.projetoresgate_api.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CnpjUtils - Util Test")
class CnpjUtilsTest {

    @Test
    @DisplayName("Deve retornar null quando o CNPJ for nulo")
    void onlyDigits_ShouldReturnNullForNull() {
        assertNull(CnpjUtils.onlyDigits(null));
    }

    @Test
    @DisplayName("Deve manter o CNPJ inalterado quando já estiver apenas com dígitos")
    void onlyDigits_ShouldKeepDigitsOnly() {
        assertEquals("11222333000181", CnpjUtils.onlyDigits("11222333000181"));
    }

    @Test
    @DisplayName("Deve remover máscara, espaços e caracteres especiais do CNPJ")
    void onlyDigits_ShouldStripMask() {
        assertEquals("11222333000181", CnpjUtils.onlyDigits("11.222.333/0001-81"));
    }

    @Test
    @DisplayName("Deve retornar string vazia quando o CNPJ tiver apenas caracteres não numéricos")
    void onlyDigits_ShouldReturnEmptyForNonDigits() {
        assertEquals("", CnpjUtils.onlyDigits("abc.-/ "));
    }

    @Test
    @DisplayName("Deve retornar string vazia quando o CNPJ for vazio")
    void onlyDigits_ShouldReturnEmptyForEmpty() {
        assertEquals("", CnpjUtils.onlyDigits(""));
    }
}