package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@DisplayName("NaturalPerson - local de trabalho - Integração")
class NaturalPersonWorkplaceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private NaturalPersonRepository repository;

    @Autowired
    private LegalPersonRepository legalPersonRepository;

    @Autowired
    private EntityManager entityManager;

    private LegalPerson givenCompany() {
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null,
                "Centro", "Fortaleza", "CE");
        return legalPersonRepository.save(LegalPerson.create("40000000000001", "Empresa X", null, "Empresa X",
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
    }

    @Test
    @DisplayName("Deve gravar, ler, trocar e remover o local de trabalho")
    void shouldPersistWorkplace() {
        LegalPerson company = givenCompany();
        NaturalPerson person = repository.save(NaturalPerson.create("Ana", "ana@email.com", null, "52998224725",
                null, null, null, null, null, company.getId()));
        entityManager.flush();
        entityManager.clear();

        NaturalPerson loaded = repository.findByIdOrThrow(person.getId());
        assertEquals(company.getId(), loaded.getWorkplaceId());

        loaded.update().workplaceId(null).apply();
        repository.saveAndFlush(loaded);
        entityManager.clear();

        assertNull(repository.findByIdOrThrow(person.getId()).getWorkplaceId());
    }
}
