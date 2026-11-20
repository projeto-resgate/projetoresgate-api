package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerParticipationStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("FindAllLearners - Integração")
class FindAllLearnersIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger CPF_SEQUENCE = new AtomicInteger(0);

    @Autowired
    private LearnerRepository learnerRepository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ClassLearnerRepository classLearnerRepository;

    @Autowired
    private ClassGroupRepository classGroupRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private LegalPersonRepository legalPersonRepository;

    private FindAllLearnersService service;

    @BeforeEach
    void setUp() {
        FindLegalPersonSummariesUseCase summaries = ids -> List.of();
        service = new FindAllLearnersService(
                learnerRepository, naturalPersonRepository, summaries, classLearnerRepository);
    }

    private Learner givenLearner(String name, String nickname, Integer academicYear) {
        int sequence = CPF_SEQUENCE.incrementAndGet();
        NaturalPerson person = naturalPersonRepository.save(NaturalPerson.create(
                name, "p" + sequence + "@email.com", nickname, "11144477" + String.format("%03d", sequence),
                null, null, null, null, null));
        return learnerRepository.save(
                Learner.create(person.getId(), null, null, false, null, academicYear, null, null));
    }

    @Test
    @DisplayName("Deve filtrar pelo nome da pessoa física")
    void handle_ShouldFilterBySearchTerm() {
        givenLearner("Ana Souza", "aninha", 2026);
        givenLearner("Bruno Lima", "bruno", 2026);
        entityManager.flush();
        entityManager.clear();

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery("ana", null, null, null, null, PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Ana Souza", result.getContent().get(0).name());
        assertEquals(LearnerParticipationStatus.NO_CLASS, result.getContent().get(0).status());
    }

    @Test
    @DisplayName("Deve filtrar pelo apelido da pessoa física")
    void handle_ShouldFilterByNickname() {
        givenLearner("Ana Souza", "aninha", 2026);
        givenLearner("Bruno Lima", "bruno", 2026);
        entityManager.flush();
        entityManager.clear();

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery("aninha", null, null, null, null, PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Ana Souza", result.getContent().get(0).name());
    }

    @Test
    @DisplayName("Deve filtrar pelo status do educando")
    void handle_ShouldFilterByStatus() {
        Learner dropped = givenLearner("Ana Souza", null, 2026);
        givenLearner("Bruno Lima", null, 2026);
        dropped.dropOut("Mudou de cidade", LocalDate.now());
        learnerRepository.save(dropped);
        entityManager.flush();
        entityManager.clear();

        Page<LearnerSummaryResponse> result = service.handle(new FindAllLearnersQuery(
                null, LearnerParticipationStatus.DROPPED_OUT, null, null, null, PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Ana Souza", result.getContent().get(0).name());
        assertEquals(LearnerParticipationStatus.DROPPED_OUT, result.getContent().get(0).status());
    }

    @Test
    @DisplayName("Deve filtrar pelo ano letivo")
    void handle_ShouldFilterByAcademicYear() {
        givenLearner("Ana Souza", null, 2025);
        givenLearner("Bruno Lima", null, 2026);
        entityManager.flush();
        entityManager.clear();

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, 2025, PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Ana Souza", result.getContent().get(0).name());
        assertEquals(2025, result.getContent().get(0).academicYear());
    }

    @Test
    @DisplayName("Deve devolver todos os educandos quando não há filtro")
    void handle_ShouldReturnAllWithoutFilters() {
        givenLearner("Ana Souza", null, 2026);
        givenLearner("Bruno Lima", null, 2026);
        entityManager.flush();
        entityManager.clear();

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, PageRequest.of(0, 10)));

        assertEquals(2, result.getTotalElements());
    }

    private ClassGroup givenClassGroup(LocalDateTime startDate, int durationInMonths, boolean active) {
        int sequence = CPF_SEQUENCE.incrementAndGet();
        Program program = programRepository.save(
                Program.create("Programa " + sequence, null, ProgramStatus.ACTIVE, null));
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null,
                "Centro", "Fortaleza", "CE");
        LegalPerson institution = legalPersonRepository.save(LegalPerson.create(
                String.format("%014d", 20000000000000L + sequence), "Escola " + sequence, null, "Escola " + sequence,
                null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
        ClassGroup classGroup = ClassGroup.create(program.getId(), institution.getId(), 20, startDate,
                durationInMonths, null, "Turma " + sequence, List.of());
        if (!active) {
            classGroup.suspend();
        }
        return classGroupRepository.save(classGroup);
    }

    private Learner givenLearnerInClass(String name, ClassGroup classGroup, boolean linkActive) {
        Learner learner = givenLearner(name, null, 2026);
        ClassLearner link = ClassLearner.create(classGroup, learner.getId());
        if (!linkActive) {
            link.deactivate();
        }
        classLearnerRepository.save(link);
        return learner;
    }

    private Page<LearnerSummaryResponse> findByStatus(LearnerParticipationStatus status) {
        entityManager.flush();
        entityManager.clear();
        return service.handle(new FindAllLearnersQuery(null, status, null, null, null, PageRequest.of(0, 50)));
    }

    private static List<String> names(Page<LearnerSummaryResponse> page) {
        return page.getContent().stream().map(LearnerSummaryResponse::name).sorted().toList();
    }

    @Test
    @DisplayName("Deve calcular ACTIVE, NO_CLASS e DROPPED_OUT e filtrar por cada um")
    void handle_ShouldComputeAndFilterParticipationStatus() {
        LocalDateTime now = LocalDateTime.now();
        ClassGroup running = givenClassGroup(now.minusMonths(1), 6, true);
        ClassGroup future = givenClassGroup(now.plusMonths(2), 6, true);
        ClassGroup finished = givenClassGroup(now.minusMonths(10), 2, true);
        ClassGroup suspended = givenClassGroup(now.minusMonths(1), 6, false);

        givenLearnerInClass("A Cursando", running, true);
        givenLearnerInClass("B Turma futura", future, true);
        givenLearnerInClass("C Turma encerrada", finished, true);
        givenLearnerInClass("D Turma suspensa", suspended, true);
        givenLearnerInClass("E Vinculo inativo", running, false);
        givenLearner("F Sem turma", null, 2026);
        Learner dropped = givenLearnerInClass("G Desistente", running, true);
        dropped.dropOut("Mudou de cidade", LocalDate.now());
        learnerRepository.save(dropped);

        assertEquals(List.of("A Cursando"), names(findByStatus(LearnerParticipationStatus.ACTIVE)));
        assertEquals(
                List.of("B Turma futura", "C Turma encerrada", "D Turma suspensa", "E Vinculo inativo", "F Sem turma"),
                names(findByStatus(LearnerParticipationStatus.NO_CLASS)));
        assertEquals(List.of("G Desistente"), names(findByStatus(LearnerParticipationStatus.DROPPED_OUT)));

        Page<LearnerSummaryResponse> all = findByStatus(null);
        assertEquals(7, all.getTotalElements());
        all.getContent().forEach(item -> assertEquals(
                switch (item.name().substring(0, 1)) {
                    case "A" -> LearnerParticipationStatus.ACTIVE;
                    case "G" -> LearnerParticipationStatus.DROPPED_OUT;
                    default -> LearnerParticipationStatus.NO_CLASS;
                }, item.status(), item.name()));
    }

    @Test
    @DisplayName("Educando em duas turmas, uma em andamento e outra não, aparece uma única vez como ACTIVE")
    void handle_ShouldNotDuplicateLearnerWithManyClasses() {
        LocalDateTime now = LocalDateTime.now();
        Learner learner = givenLearnerInClass("Ana", givenClassGroup(now.minusMonths(1), 6, true), true);
        classLearnerRepository.save(ClassLearner.create(givenClassGroup(now.minusMonths(1), 6, true), learner.getId()));
        classLearnerRepository.save(ClassLearner.create(givenClassGroup(now.minusMonths(12), 2, true), learner.getId()));

        Page<LearnerSummaryResponse> result = findByStatus(LearnerParticipationStatus.ACTIVE);

        assertEquals(1, result.getTotalElements());
        assertEquals(LearnerParticipationStatus.ACTIVE, result.getContent().get(0).status());
    }
}
