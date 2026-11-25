package com.projetoresgate.projetoresgate_api.shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("UrlUtils - Test")
class UrlUtilsTest {

    @Test
    @DisplayName("Deve remover uma ou várias barras do final")
    void removeTrailingSlashes_ShouldStripAllTrailingSlashes() {
        assertEquals("https://app.exemplo.org", UrlUtils.removeTrailingSlashes("https://app.exemplo.org/"));
        assertEquals("https://app.exemplo.org", UrlUtils.removeTrailingSlashes("https://app.exemplo.org///"));
    }

    @Test
    @DisplayName("Não deve alterar URL sem barra no final nem barras no meio")
    void removeTrailingSlashes_ShouldKeepOtherUrls() {
        assertEquals("https://app.exemplo.org", UrlUtils.removeTrailingSlashes("https://app.exemplo.org"));
        assertEquals("https://app.exemplo.org/base", UrlUtils.removeTrailingSlashes("https://app.exemplo.org/base/"));
    }

    @Test
    @DisplayName("Deve aceitar texto vazio ou só barras")
    void removeTrailingSlashes_ShouldHandleEmptyAndSlashesOnly() {
        assertEquals("", UrlUtils.removeTrailingSlashes(""));
        assertEquals("", UrlUtils.removeTrailingSlashes("////"));
    }
}
