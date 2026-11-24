package com.projetoresgate.projetoresgate_api.shared;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
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
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Leitura de registros excluídos (para exibição) - Integração")
class DeletedRecordsLookupIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Autowired private ProgramRepository programRepository;
    @Autowired private EducatorCategoryItemRepository categoryRepository;
    @Autowired private NaturalPersonRepository naturalPersonRepository;
    @Autowired private LegalPersonRepository legalPersonRepository;
    @Autowired private LearnerRepository learnerRepository;
    @Autowired private CollaboratorRepository collaboratorRepository;
    @Autowired private EntityManager entityManager;

    private void deleteAndFlush(Runnable delete) {
        delete.run();
        entityManager.flush();
        entityManager.clear();
    }

    private NaturalPerson givenPerson(String name) {
        int n = SEQUENCE.incrementAndGet();
        return naturalPersonRepository.save(NaturalPerson.create(
                name, "pessoa" + n + "@example.com", null, null, null, null, null, null, null));
    }

    private LegalPerson givenCompany(String displayName) {
        int n = SEQUENCE.incrementAndGet();
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null, "Centro", "Fortaleza", "CE");
        return legalPersonRepository.save(LegalPerson.create(String.format("%014d", 40000000000000L + n),
                displayName, displayName, displayName, null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                address, null));
    }

    @Test
    @DisplayName("Programa excluído: some do finder comum, mas o nome continua legível para exibição")
    void program() {
        Program program = programRepository.save(Program.create("Reforço Escolar", null, ProgramStatus.ACTIVE, null));
        UUID id = program.getId();

        deleteAndFlush(() -> programRepository.delete(program));

        assertTrue(programRepository.findById(id).isEmpty(), "finder comum não enxerga excluído");
        assertTrue(programRepository.findAllById(List.of(id)).isEmpty());
        assertEquals("Reforço Escolar", programRepository.findByIdIncludingDeleted(id).orElseThrow().getName());
        assertEquals(1, programRepository.findAllByIdIncludingDeleted(List.of(id)).size());
    }

    @Test
    @DisplayName("Categoria de educador (excluída junto com o programa) continua legível")
    void educatorCategory() {
        Program program = Program.create("Arte", null, ProgramStatus.ACTIVE, null);
        program.getEducatorCategoryItemList().add(EducatorCategoryItem.create("Professor", program));
        programRepository.save(program);
        entityManager.flush();
        UUID categoryId = program.getEducatorCategoryItemList().get(0).getId();

        deleteAndFlush(() -> programRepository.delete(program));

        assertTrue(categoryRepository.findById(categoryId).isEmpty());
        assertEquals("Professor",
                categoryRepository.findByIdIncludingDeleted(categoryId).orElseThrow().getName());
    }

    @Test
    @DisplayName("Pessoa física excluída continua legível para exibição")
    void naturalPerson() {
        NaturalPerson person = givenPerson("Maria da Silva");
        UUID id = person.getId();

        deleteAndFlush(() -> naturalPersonRepository.delete(person));

        assertTrue(naturalPersonRepository.findById(id).isEmpty());
        assertEquals("Maria da Silva", naturalPersonRepository.findByIdIncludingDeleted(id).orElseThrow().getName());
    }

    @Test
    @DisplayName("Empresa excluída (com o endereço também excluído) continua legível, sem estourar")
    void legalPerson() {
        LegalPerson company = givenCompany("Escola Esperança");
        UUID id = company.getId();

        deleteAndFlush(() -> legalPersonRepository.delete(company));

        assertTrue(legalPersonRepository.findById(id).isEmpty());
        LegalPerson found = assertDoesNotThrow(() -> legalPersonRepository.findByIdIncludingDeleted(id).orElseThrow());
        assertEquals("Escola Esperança", found.getDisplayName());
        assertNull(found.getAddress(), "o endereço foi excluído junto e fica oculto");
    }

    @Test
    @DisplayName("Educando excluído continua legível para exibição")
    void learner() {
        NaturalPerson person = givenPerson("Pedro Aluno");
        Learner learner = learnerRepository.save(Learner.create(person.getId(), null, null, false, null, 2026, null, null));
        UUID id = learner.getId();

        deleteAndFlush(() -> learnerRepository.delete(learner));

        assertTrue(learnerRepository.findById(id).isEmpty());
        assertEquals(person.getId(), learnerRepository.findByIdIncludingDeleted(id).orElseThrow().getNaturalPersonId());
    }

    @Test
    @DisplayName("Colaborador excluído continua legível para exibição")
    void collaborator() {
        NaturalPerson person = givenPerson("Ana Voluntária");
        Collaborator collaborator = collaboratorRepository.save(Collaborator.create(true, person.getId(), null));
        UUID id = collaborator.getId();

        deleteAndFlush(() -> collaboratorRepository.delete(collaborator));

        assertTrue(collaboratorRepository.findById(id).isEmpty());
        assertEquals(person.getId(), collaboratorRepository.findByIdIncludingDeleted(id).orElseThrow().getNaturalPersonId());
    }

    @Test
    @DisplayName("Id inexistente devolve vazio, e lista vazia de ids não quebra")
    void unknownId() {
        assertTrue(programRepository.findByIdIncludingDeleted(UUID.randomUUID()).isEmpty());
        assertTrue(programRepository.findAllByIdIncludingDeleted(List.of(UUID.randomUUID())).isEmpty());
    }
}
