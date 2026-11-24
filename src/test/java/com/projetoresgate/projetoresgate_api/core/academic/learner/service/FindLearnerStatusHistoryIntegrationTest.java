package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerStatusHistoryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerStatusHistoryRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerStatusHistoryQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("FindLearnerStatusHistory - Integração")
class FindLearnerStatusHistoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private LearnerRepository learnerRepository;

    @Autowired
    private LearnerStatusHistoryRepository historyRepository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private EntityManager entityManager;

    private FindLearnerStatusHistoryService service;

    @BeforeEach
    void setUp() {
        service = new FindLearnerStatusHistoryService(learnerRepository, historyRepository);
    }

    private Learner givenLearner(String cpf) {
        NaturalPerson person = naturalPersonRepository.save(NaturalPerson.create(
                "Ana Souza", cpf + "@email.com", null, cpf, null, null, null, null, null));
        return learnerRepository.save(Learner.create(person.getId(), null, null, false, null, 2026, null, null));
    }

    @Test
    @DisplayName("Deve listar desistência e retomada com motivo e datas, do mais recente para o mais antigo")
    void handle_ShouldListEventsNewestFirst() {
        Learner learner = givenLearner("52998224725");
        learner.dropOut("Mudou de cidade", LocalDate.of(2026, 3, 10));
        learner.reactivate("Voltou", LocalDate.of(2026, 8, 1));
        learner.dropOut("Problemas de saúde", LocalDate.of(2026, 10, 5));
        learnerRepository.save(learner);
        entityManager.flush();
        entityManager.clear();

        List<LearnerStatusHistoryResponse> result =
                service.handle(new FindLearnerStatusHistoryQuery(learner.getId()));

        assertEquals(3, result.size());
        assertEquals(LearnerStatusEventType.DROPOUT, result.get(0).type());
        assertEquals("Problemas de saúde", result.get(0).reason());
        assertEquals(LocalDate.of(2026, 10, 5), result.get(0).eventDate());
        assertNotNull(result.get(0).registeredAt());
        assertEquals(LearnerStatusEventType.REACTIVATION, result.get(1).type());
        assertEquals("Voltou", result.get(1).reason());
        assertEquals(LearnerStatusEventType.DROPOUT, result.get(2).type());
        assertEquals("Mudou de cidade", result.get(2).reason());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o educando nunca desistiu")
    void handle_ShouldReturnEmptyWhenNoEvents() {
        Learner learner = givenLearner("11144477735");
        entityManager.flush();
        entityManager.clear();

        assertTrue(service.handle(new FindLearnerStatusHistoryQuery(learner.getId())).isEmpty());
    }

    @Test
    @DisplayName("Não deve devolver o histórico de outro educando")
    void handle_ShouldNotMixLearners() {
        Learner first = givenLearner("52998224725");
        Learner second = givenLearner("11144477735");
        first.dropOut("Motivo A", LocalDate.of(2026, 3, 10));
        second.dropOut("Motivo B", LocalDate.of(2026, 4, 10));
        learnerRepository.save(first);
        learnerRepository.save(second);
        entityManager.flush();
        entityManager.clear();

        List<LearnerStatusHistoryResponse> result = service.handle(new FindLearnerStatusHistoryQuery(first.getId()));

        assertEquals(1, result.size());
        assertEquals("Motivo A", result.get(0).reason());
    }

    @Test
    @DisplayName("Deve falhar com educando inexistente")
    void handle_ShouldFailWhenLearnerNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new FindLearnerStatusHistoryQuery(UUID.randomUUID())));
    }
}
