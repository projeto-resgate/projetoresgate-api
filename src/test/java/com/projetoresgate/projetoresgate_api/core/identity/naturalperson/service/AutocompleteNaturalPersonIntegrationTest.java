package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.AutocompleteNaturalPersonQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("AutocompleteNaturalPersonService - Integração (JPA + H2)")
class AutocompleteNaturalPersonIntegrationTest {

    @Autowired
    private NaturalPersonRepository repository;

    private AutocompleteNaturalPersonService service;

    @BeforeEach
    void setUp() {
        service = new AutocompleteNaturalPersonService(repository);

        repository.saveAllAndFlush(List.of(
                NaturalPerson.create("João da Silva", "joao@test.com", "joaosilva", "51086174968", "12345678",
                        LocalDate.of(1990, 1, 10), Gender.MALE, "1133334444", "11988888888"),
                NaturalPerson.create("Maria Souza", "maria@test.com", "maria", "11144477735", "87654321",
                        LocalDate.of(1985, 5, 20), Gender.FEMALE, "2133335555", "21977777777"),
                NaturalPerson.create("Pedro Almeida", "pedro@test.com", "pedro", "52998224725", "55111222",
                        LocalDate.of(2000, 12, 1), Gender.MALE, "3133336666", "31966666666")
        ));
    }

    @Test
    @DisplayName("Deve encontrar por nome parcial")
    void handle_ShouldFindByName() {
        List<NaturalPersonSummaryResponse> result = service.handle(new AutocompleteNaturalPersonQuery("joão", 10));

        assertEquals(1, result.size());
        assertEquals("João da Silva", result.get(0).name());
        assertEquals("51086174968", result.get(0).cpf());
    }

    @Test
    @DisplayName("Deve encontrar por nickname parcial")
    void handle_ShouldFindByNickname() {
        List<NaturalPersonSummaryResponse> result = service.handle(new AutocompleteNaturalPersonQuery("maria", 10));

        assertEquals(1, result.size());
        assertEquals("Maria Souza", result.get(0).name());
        assertEquals("11144477735", result.get(0).cpf());
    }

    @Test
    @DisplayName("Deve encontrar por dígitos do CPF")
    void handle_ShouldFindByCpfDigits() {
        List<NaturalPersonSummaryResponse> result = service.handle(new AutocompleteNaturalPersonQuery("529.982.247-25", 10));

        assertEquals(1, result.size());
        assertEquals("Pedro Almeida", result.get(0).name());
    }

    @Test
    @DisplayName("Deve encontrar por RG parcial")
    void handle_ShouldFindByRg() {
        List<NaturalPersonSummaryResponse> result = service.handle(new AutocompleteNaturalPersonQuery("654321", 10));

        assertEquals(1, result.size());
        assertEquals("Maria Souza", result.get(0).name());
        assertEquals("87654321", result.get(0).rg());
    }

    @Test
    @DisplayName("Deve retornar o top-N ordenado por nome mesmo com searchTerm em branco")
    void handle_ShouldReturnTopNWhenBlankSearchTerm() {
        List<NaturalPersonSummaryResponse> result = service.handle(new AutocompleteNaturalPersonQuery("   ", 10));

        assertEquals(3, result.size());
        assertEquals("João da Silva", result.get(0).name());
        assertEquals("Maria Souza", result.get(1).name());
        assertEquals("Pedro Almeida", result.get(2).name());
    }
}