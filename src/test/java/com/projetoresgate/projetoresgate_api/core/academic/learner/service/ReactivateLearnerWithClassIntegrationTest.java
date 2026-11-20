package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.service.ActivateClassLearnerLinkService;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ReactivateLearnerCommand;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("ReactivateLearner com turma - Integração")
class ReactivateLearnerWithClassIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private LearnerRepository learnerRepository;
    @Autowired
    private NaturalPersonRepository naturalPersonRepository;
    @Autowired
    private ClassLearnerRepository classLearnerRepository;
    @Autowired
    private ClassGroupRepository classGroupRepository;
    @Autowired
    private ProgramRepository programRepository;
    @Autowired
    private LegalPersonRepository legalPersonRepository;
    @Autowired
    private EntityManager entityManager;

    private ReactivateLearnerService service;
    private ClassGroup classGroup;
    private ClassGroup otherClassGroup;
    private Learner learner;

    private ClassGroup givenClassGroup(int sequence) {
        Program program = programRepository.save(Program.create("Programa " + sequence, null, ProgramStatus.ACTIVE, null));
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null,
                "Centro", "Fortaleza", "CE");
        LegalPerson institution = legalPersonRepository.save(LegalPerson.create(
                String.format("%014d", 30000000000000L + sequence), "Escola " + sequence, null, "Escola " + sequence,
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
        return classGroupRepository.save(ClassGroup.create(program.getId(), institution.getId(), 20,
                LocalDateTime.now().minusMonths(1), 6, null, "Turma " + sequence, List.of()));
    }

    @BeforeEach
    void setUp() {
        service = new ReactivateLearnerService(learnerRepository, query -> null,
                new ActivateClassLearnerLinkService(classGroupRepository, classLearnerRepository));

        classGroup = givenClassGroup(1);
        otherClassGroup = givenClassGroup(2);

        NaturalPerson person = naturalPersonRepository.save(NaturalPerson.create(
                "Ana Souza", "ana@email.com", null, "52998224725", null, null, null, null, null));
        learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        learner.dropOut("Mudou de cidade", LocalDate.of(2026, 3, 10));
        learnerRepository.save(learner);
        classLearnerRepository.save(ClassLearner.create(classGroup, learner.getId()));
        classLearnerRepository.deactivateByLearnerId(learner.getId());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Reativar com turma reativa o cadastro e o vínculo existente na turma informada")
    void shouldReactivateLearnerAndExistingLink() {
        service.handle(new ReactivateLearnerCommand(learner.getId(), "Voltou", classGroup.getId()));
        entityManager.flush();
        entityManager.clear();

        assertEquals(LearnerStatus.ACTIVE, learnerRepository.findByIdOrThrow(learner.getId()).getStatus());
        ClassLearner link = classLearnerRepository
                .findByClassGroupIdAndLearnerId(classGroup.getId(), learner.getId()).orElseThrow();
        assertTrue(link.isActive());
        assertEquals(1, classLearnerRepository.findAll().stream()
                .filter(l -> l.getLearnerId().equals(learner.getId())).count());
    }

    @Test
    @DisplayName("Educando já ativo pode ser ativado em outra turma sem erro de 'já é ativo'")
    void shouldActivateInAnotherClassWhenAlreadyActive() {
        service.handle(new ReactivateLearnerCommand(learner.getId(), null, classGroup.getId()));
        entityManager.flush();
        entityManager.clear();

        service.handle(new ReactivateLearnerCommand(learner.getId(), null, otherClassGroup.getId()));
        entityManager.flush();
        entityManager.clear();

        assertTrue(classLearnerRepository.findByClassGroupIdAndLearnerId(classGroup.getId(), learner.getId())
                .orElseThrow().isActive());
        assertTrue(classLearnerRepository.findByClassGroupIdAndLearnerId(otherClassGroup.getId(), learner.getId())
                .orElseThrow().isActive());
        assertEquals(2, learnerRepository.findByIdOrThrow(learner.getId()).getStatusHistory().size());
    }

    @Test
    @DisplayName("Reativar sem turma, já ativo, continua retornando erro")
    void shouldStillFailWhenAlreadyActiveWithoutClass() {
        service.handle(new ReactivateLearnerCommand(learner.getId(), null, null));
        entityManager.flush();
        entityManager.clear();

        assertThrows(IllegalStateException.class,
                () -> service.handle(new ReactivateLearnerCommand(learner.getId(), null, null)));
    }

    @Test
    @DisplayName("Turma inexistente desfaz a reativação")
    void shouldFailForUnknownClassGroup() {
        assertThrows(RuntimeException.class,
                () -> service.handle(new ReactivateLearnerCommand(learner.getId(), null, UUID.randomUUID())));
    }
}
