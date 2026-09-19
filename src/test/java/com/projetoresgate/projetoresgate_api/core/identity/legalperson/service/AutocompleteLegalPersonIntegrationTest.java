package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.AutocompleteLegalPersonQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("AutocompleteLegalPersonService - Integração")
class AutocompleteLegalPersonIntegrationTest {

    @Autowired
    private LegalPersonRepository repository;

    private AutocompleteLegalPersonService service;

    @BeforeEach
    void setUp() {
        service = new AutocompleteLegalPersonService(repository);

        repository.saveAllAndFlush(List.of(
                LegalPerson.create("11222333000181", "Razão Social Acme LTDA", "Acme Fantasia", "Acme LTDA", "6201-5/00",
                        RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                        Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP"), null),
                LegalPerson.create("98765432000198", "Beta Comércio e Serviços", "Beta Store", "Beta Store", "4711-8/02",
                        RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                        Address.create("20040-020", "200", null, "Centro", "Rio de Janeiro", "RJ"), null),
                LegalPerson.create("12345678000190", "Gama Tecnologia", "Gama Tech", "Gama Tech", "6201-5/00",
                        RegistrationStatus.SUSPENDED, CompanyStatus.INACTIVE,
                        Address.create("30130-010", "300", null, "Funcionários", "Belo Horizonte", "MG"), null)
        ));
    }

    @Test
    @DisplayName("Deve encontrar por razão social parcial")
    void handle_ShouldFindByCorporateName() {
        List<LegalPersonSummaryResponse> result = service.handle(new AutocompleteLegalPersonQuery("acme", 10));

        assertEquals(1, result.size());
        assertEquals("Acme LTDA", result.get(0).displayName());
        assertEquals("11222333000181", result.get(0).cnpj());
    }

    @Test
    @DisplayName("Deve encontrar por nome fantasia parcial")
    void handle_ShouldFindByTradeName() {
        List<LegalPersonSummaryResponse> result = service.handle(new AutocompleteLegalPersonQuery("beta", 10));

        assertEquals(1, result.size());
        assertEquals("Beta Store", result.get(0).displayName());
        assertEquals("98765432000198", result.get(0).cnpj());
    }

    @Test
    @DisplayName("Deve encontrar por CNPJ com máscara mandando apenas os dígitos")
    void handle_ShouldFindByCnpjDigits() {
        List<LegalPersonSummaryResponse> result = service.handle(new AutocompleteLegalPersonQuery("11.222.333/0001-81", 10));

        assertEquals(1, result.size());
        assertEquals("Acme LTDA", result.get(0).displayName());
        assertEquals("11222333000181", result.get(0).cnpj());
    }

    @Test
    @DisplayName("Deve respeitar o limite e ordenar por displayName ascendente mesmo sem termo")
    void handle_ShouldRespectLimitAndSortByDisplayName() {
        List<LegalPersonSummaryResponse> limited = service.handle(new AutocompleteLegalPersonQuery("", 2));

        assertEquals(2, limited.size());
        assertEquals("Acme LTDA", limited.get(0).displayName());
        assertEquals("Beta Store", limited.get(1).displayName());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhum resultado corresponde")
    void handle_ShouldReturnEmptyWhenNoMatch() {
        List<LegalPersonSummaryResponse> result = service.handle(new AutocompleteLegalPersonQuery("xyz nao existe", 10));

        assertTrue(result.isEmpty());
    }
}