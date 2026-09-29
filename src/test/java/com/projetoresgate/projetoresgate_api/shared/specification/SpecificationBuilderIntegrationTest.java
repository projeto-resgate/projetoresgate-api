package com.projetoresgate.projetoresgate_api.shared.specification;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("SpecificationBuilder - Integração (JPA + Postgres)")
class SpecificationBuilderIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private NaturalPersonRepository repository;

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @BeforeEach
    void setUp() {
        repository.saveAllAndFlush(List.of(
                NaturalPerson.create("João da Silva", "joao@test.com", "joaosilva", "51086174968", "12345678",
                        LocalDate.of(1990, 1, 10), Gender.MALE, "1133334444", "11988888888"),
                NaturalPerson.create("Maria Souza", "maria@test.com", "maria", "11144477735", "87654321",
                        LocalDate.of(1985, 5, 20), Gender.FEMALE, "2133335555", "21977777777"),
                NaturalPerson.create("Pedro Almeida", "pedro@test.com", "pedro", "52998224725", "55111222",
                        LocalDate.of(2000, 12, 1), Gender.MALE, "3133336666", "31966666666")
        ));
    }

    private List<NaturalPerson> findBy(Specification<NaturalPerson> spec) {
        return repository.findAll(spec);
    }

    @Test
    @DisplayName("Propriedade aninhada com ponto deve ser resolvida")
    void withNestedProperty_ResolvesPath() {
        Specification<FamilyGroup> spec = new SpecificationBuilder<FamilyGroup>()
                .with("address.city", "~", "SP")
                .build();

        assertTrue(familyGroupRepository.findAll(spec).isEmpty());
    }

    @Test
    @DisplayName("Vários filtros devem ser combinados com E")
    void withSeveralFilters_AppliesAllOfThem() {
        List<NaturalPerson> result = findBy(new SpecificationBuilder<NaturalPerson>()
                .with("name", "~", "a")
                .with("gender", ":", Gender.MALE)
                .build());

        assertEquals(2, result.size());
    }

    @Nested
    @DisplayName("Operação de igualdade (:)")
    class Equality {

        @Test
        @DisplayName("Em String deve casar o valor exato")
        void withString_MatchesWholeValueOnly() {
            List<NaturalPerson> byExactName = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", ":", "Maria Souza")
                    .build());

            assertEquals(1, byExactName.size());
            assertEquals("Maria Souza", byExactName.get(0).getName());
        }

        @Test
        @DisplayName("Em String deve diferenciar a caixa, ao contrário do ILIKE")
        void withString_IsCaseSensitive() {
            List<NaturalPerson> byDifferentCase = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", ":", "maria souza")
                    .build());

            assertTrue(byDifferentCase.isEmpty());
        }

        @Test
        @DisplayName("Em String não deve casar valor parcial, ao contrário do ILIKE")
        void withString_DoesNotMatchPartialValue() {
            List<NaturalPerson> byPartialName = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", ":", "Maria")
                    .build());

            assertTrue(byPartialName.isEmpty());
        }

        @Test
        @DisplayName("Em enum deve casar o valor exato")
        void withEnum_MatchesExactValue() {
            List<NaturalPerson> females = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("gender", ":", Gender.FEMALE)
                    .build());

            assertEquals(1, females.size());
            assertEquals("Maria Souza", females.get(0).getName());
        }
    }

    @Nested
    @DisplayName("Operação de ILIKE (~)")
    class ILike {

        @Test
        @DisplayName("Deve casar trecho no meio do valor, ignorando a caixa")
        void withString_MatchesFragmentAnywhere() {
            List<NaturalPerson> byFragment = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", "MARIA")
                    .build());

            assertEquals(1, byFragment.size());
            assertEquals("Maria Souza", byFragment.get(0).getName());
        }

        @Test
        @DisplayName("Deve casar também no início e no fim do valor")
        void withString_MatchesAtBothEdges() {
            assertEquals(1, findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", "João")
                    .build()).size());

            assertEquals(1, findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", "Almeida")
                    .build()).size());
        }

        @Test
        @DisplayName("Deve casar dígitos no meio do valor")
        void withString_MatchesDigitsInsideValue() {
            List<NaturalPerson> byRg = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("rg", "~", "654321")
                    .build());

            assertEquals(1, byRg.size());
            assertEquals("87654321", byRg.get(0).getRg());
        }

        @Test
        @DisplayName("Deve tratar % do valor como texto literal, não como curinga")
        void withString_EscapesPercentSign() {
            List<NaturalPerson> byLiteralPercent = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", "%")
                    .build());

            assertTrue(byLiteralPercent.isEmpty());
        }

        @Test
        @DisplayName("Deve tratar _ do valor como texto literal, não como curinga de um caractere")
        void withString_EscapesUnderscore() {
            List<NaturalPerson> byLiteralUnderscore = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", "_")
                    .build());

            assertTrue(byLiteralUnderscore.isEmpty());
        }

        @Test
        @DisplayName("Deve recusar propriedade que não é texto, com mensagem apontando para :")
        void withNonStringProperty_Throws() {
            Specification<NaturalPerson> specification = new SpecificationBuilder<NaturalPerson>()
                    .with("gender", "~", Gender.FEMALE)
                    .build();

            var exception = assertThrows(InvalidDataAccessApiUsageException.class,
                    () -> findBy(specification));

            assertThat(exception.getMessage())
                    .contains("~")
                    .contains("gender")
                    .contains(":");
        }
    }

    @Nested
    @DisplayName("Operações de comparação")
    class Comparison {

        @Test
        @DisplayName("Maior que em data deve excluir o próprio valor")
        void greaterThan_OnDate_ExcludesEqualValue() {
            List<NaturalPerson> bornAfter2000 = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("birthDate", ">", LocalDate.of(2000, 1, 1))
                    .build());

            assertEquals(1, bornAfter2000.size());
            assertEquals("Pedro Almeida", bornAfter2000.get(0).getName());
        }

        @Test
        @DisplayName("Maior ou igual em data deve incluir o próprio valor")
        void greaterOrEqual_OnDate_IncludesEqualValue() {
            List<NaturalPerson> bornFrom2000 = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("birthDate", ">=", LocalDate.of(2000, 1, 1))
                    .build());

            assertEquals(1, bornFrom2000.size());
            assertEquals("Pedro Almeida", bornFrom2000.get(0).getName());
        }

        @Test
        @DisplayName("Menor que em data deve excluir o próprio valor")
        void lessThan_OnDate_ExcludesEqualValue() {
            List<NaturalPerson> bornBefore2000 = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("birthDate", "<", LocalDate.of(2000, 1, 1))
                    .build());

            assertEquals(2, bornBefore2000.size());
        }

        @Test
        @DisplayName("Menor ou igual em data deve incluir o próprio valor")
        void lessOrEqual_OnDate_IncludesEqualValue() {
            List<NaturalPerson> bornUntil2000 = findBy(new SpecificationBuilder<NaturalPerson>()
                    .with("birthDate", "<=", LocalDate.of(2000, 1, 1))
                    .build());

            assertEquals(2, bornUntil2000.size());
        }
    }

    @Nested
    @DisplayName("Valores ausentes e entrada inválida")
    class InputValidation {

        @Test
        @DisplayName("build() sem nenhum filtro deve devolver null")
        void build_WithoutFilters_ReturnsNull() {
            assertNull(new SpecificationBuilder<NaturalPerson>().build());
        }

        @Test
        @DisplayName("Filtro com valor null deve ser ignorado")
        void with_NullValue_IsIgnored() {
            assertNull(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", null)
                    .build());
        }

        @Test
        @DisplayName("Filtro com valor em branco deve ser ignorado")
        void with_BlankValue_IsIgnored() {
            assertNull(new SpecificationBuilder<NaturalPerson>()
                    .with("name", "~", "   ")
                    .build());
        }

        @Test
        @DisplayName("Operação desconhecida deve falhar já no with(), não na execução da query")
        void with_UnsupportedOperation_ThrowsImmediately() {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                    new SpecificationBuilder<NaturalPerson>().with("name", "=", "Maria"));

            assertEquals("Operação não suportada: \"=\"", error.getMessage());
        }
    }
}
