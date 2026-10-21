package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@DisplayName("ClassLearnerRepository - Integração")
class ClassLearnerIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Autowired
    private ClassLearnerRepository repository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private LegalPersonRepository legalPersonRepository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private LearnerRepository learnerRepository;

    @Autowired
    private EntityManager entityManager;

    private ClassGroup classGroup;

    @BeforeEach
    void setUp() {
        int sequence = SEQUENCE.incrementAndGet();

        Program program = programRepository.save(Program.create("Programa " + sequence, null, ProgramStatus.ACTIVE, null));
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null,
                "Centro", "Fortaleza", "CE");
        LegalPerson institution = legalPersonRepository.save(LegalPerson.create(
                String.format("%014d", 10000000000000L + sequence), "Escola " + sequence, null, "Escola " + sequence,
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));

        classGroup = classGroupRepository.save(ClassGroup.create(program.getId(), institution.getId(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Turma " + sequence, List.of()));
    }

    private Learner givenLearner(String name) {
        int sequence = SEQUENCE.incrementAndGet();
        NaturalPerson person = naturalPersonRepository.save(NaturalPerson.create(
                name, "p" + sequence + "@example.com", null, "5299822" + String.format("%04d", sequence),
                null, null, null, null, null));
        return learnerRepository.save(
                Learner.create(person.getId(), null, null, false, null, 2026, null, null));
    }

    @Test
    @DisplayName("Deve ordenar os vínculos ativos primeiro e depois pelo nome do educando")
    void findAllByClassGroupId_ShouldOrderActiveFirstThenByName() {
        Learner ana = givenLearner("Ana Souza");
        Learner bruno = givenLearner("Bruno Lima");

        ClassLearner inactive = ClassLearner.create(classGroup, ana.getId());
        inactive.deactivate();
        repository.save(inactive);
        repository.save(ClassLearner.create(classGroup, bruno.getId()));

        repository.flush();
        entityManager.clear();

        Page<ClassLearner> result = repository.findAllByClassGroupId(
                classGroup.getId(), null, null, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of(bruno.getId(), ana.getId()),
                result.getContent().stream().map(ClassLearner::getLearnerId).toList());
    }

    @Test
    @DisplayName("Deve filtrar pelo nome da pessoa física do educando")
    void findAllByClassGroupId_ShouldFilterBySearchTerm() {
        Learner ana = givenLearner("Ana Souza");
        Learner bruno = givenLearner("Bruno Lima");
        Learner carla = givenLearner("Carla Dias");

        repository.save(ClassLearner.create(classGroup, ana.getId()));
        repository.save(ClassLearner.create(classGroup, bruno.getId()));
        repository.save(ClassLearner.create(classGroup, carla.getId()));

        entityManager.flush();
        entityManager.clear();

        Page<ClassLearner> result = repository.findAllByClassGroupId(
                classGroup.getId(), null, "bruno", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(bruno.getId(), result.getContent().getFirst().getLearnerId());
    }

    @Test
    @DisplayName("Deve inativar todos os vínculos ativos do educando")
    void deactivateByLearnerId_ShouldDeactivateAllActiveLinks() {
        Learner learner = givenLearner("Ana Souza");

        ClassGroup other = classGroupRepository.save(ClassGroup.create(
                classGroup.getProgramId(), classGroup.getInstitutionId(), 20,
                LocalDateTime.of(2026, 8, 1, 8, 0), 6, null, "Outra turma", List.of()));

        repository.save(ClassLearner.create(classGroup, learner.getId()));
        repository.save(ClassLearner.create(other, learner.getId()));
        repository.flush();

        int deactivated = repository.deactivateByLearnerId(learner.getId());
        repository.flush();
        entityManager.clear();

        assertEquals(2, deactivated);

        Pageable pageable = PageRequest.of(0, 10);
        assertFalse(repository.findAllByClassGroupId(classGroup.getId(), true, null, pageable).hasContent());
        assertFalse(repository.findAllByClassGroupId(other.getId(), true, null, pageable).hasContent());
    }

    @Test
    @DisplayName("A exclusão lógica em lote deve esconder os vínculos da turma")
    void softDeleteByClassGroupId_ShouldHideLinks() {
        Learner learner = givenLearner("Ana Souza");
        repository.save(ClassLearner.create(classGroup, learner.getId()));
        repository.flush();

        repository.softDeleteByClassGroupId(classGroup.getId());
        entityManager.clear();

        assertFalse(repository.findAllByClassGroupId(
                classGroup.getId(), null, null, PageRequest.of(0, 10)).hasContent());
    }
}
