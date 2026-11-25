package com.projetoresgate.projetoresgate_api.shared.specification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Specs - Test")
class SpecsTest {

    @Test
    @DisplayName("Todas nulas: não lança exceção e devolve uma especificação sem restrição (nunca null)")
    void allOf_ShouldReturnUnrestrictedWhenAllNull() {
        assertNotNull(Specs.<String>allOf((Specification<String>) null, null));
        assertNotNull(Specs.<String>allOf());
    }

    @Test
    @DisplayName("Ignora as nulas e combina as demais")
    void allOf_ShouldCombineNonNull() {
        Specification<String> a = (root, query, cb) -> null;
        Specification<String> b = (root, query, cb) -> null;

        assertNotNull(Specs.allOf(a, null, b));
        assertNotNull(Specs.allOf(null, a));
        assertNotNull(Specs.allOf(a, null));
    }
}
