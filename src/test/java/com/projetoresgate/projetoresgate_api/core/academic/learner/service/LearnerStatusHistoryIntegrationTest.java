package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DisplayName("LearnerStatusHistory - Integração")
class LearnerStatusHistoryIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger CPF_SEQUENCE = new AtomicInteger(0);

    @Autowired
    private LearnerRepository learnerRepository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private EntityManager entityManager;

    private Learner givenLearner() {
        int sequence = CPF_SEQUENCE.incrementAndGet();
        NaturalPerson person = naturalPersonRepository.save(NaturalPerson.create(
                "Ana Desistente " + sequence, "ana." + sequence + "@email.com", null,
                "11144477" + String.format("%03d", sequence), null, null, null, null, null));
        return learnerRepository.save(
                Learner.create(person.getId(), null, null, false, null, 2026, null, null));
    }

    @Test
    @DisplayName("Deve iniciar o educando com status ativo")
    void create_ShouldPersistActiveStatus() {
        UUID id = givenLearner().getId();
        entityManager.flush();
        entityManager.clear();

        Learner reloaded = learnerRepository.findById(id).orElseThrow();

        assertEquals(LearnerStatus.ACTIVE, reloaded.getStatus());
    }

    @Test
    @DisplayName("Deve persistir a desistência com motivo e data no histórico")
    void dropOut_ShouldPersistStatusAndHistory() {
        Learner learner = givenLearner();
        UUID id = learner.getId();

        learner.dropOut("Mudou de cidade", LocalDate.of(2026, 5, 10));
        learnerRepository.save(learner);
        entityManager.flush();
        entityManager.clear();

        Learner reloaded = learnerRepository.findById(id).orElseThrow();

        assertEquals(LearnerStatus.DROPPED_OUT, reloaded.getStatus());
        assertEquals(1, reloaded.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.DROPOUT, reloaded.getStatusHistory().getFirst().getType());
        assertEquals("Mudou de cidade", reloaded.getStatusHistory().getFirst().getReason());
        assertEquals(LocalDate.of(2026, 5, 10), reloaded.getStatusHistory().getFirst().getEventDate());
    }

    @Test
    @DisplayName("Deve manter desistência e reativação no histórico relido do banco")
    void reactivate_ShouldKeepFullHistory() {
        Learner learner = givenLearner();
        UUID id = learner.getId();

        learner.dropOut("Mudou de cidade", LocalDate.of(2026, 5, 10));
        learner.reactivate("Retornou no próximo ano letivo", LocalDate.of(2027, 2, 1));
        learnerRepository.save(learner);
        entityManager.flush();
        entityManager.clear();

        Learner reloaded = learnerRepository.findById(id).orElseThrow();

        assertEquals(LearnerStatus.ACTIVE, reloaded.getStatus());
        assertEquals(2, reloaded.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.DROPOUT, reloaded.getStatusHistory().get(0).getType());
        assertEquals(LearnerStatusEventType.REACTIVATION, reloaded.getStatusHistory().get(1).getType());
        assertEquals("Retornou no próximo ano letivo", reloaded.getStatusHistory().get(1).getReason());
    }
}
