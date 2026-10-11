package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
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

    private FindAllLearnersService service;

    @BeforeEach
    void setUp() {
        FindLegalPersonSummariesUseCase summaries = ids -> List.of();
        service = new FindAllLearnersService(learnerRepository, naturalPersonRepository, summaries);
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
        assertTrue(result.getContent().get(0).active());
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
                null, LearnerStatus.DROPPED_OUT, null, null, null, PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Ana Souza", result.getContent().get(0).name());
        assertFalse(result.getContent().get(0).active());
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
}
