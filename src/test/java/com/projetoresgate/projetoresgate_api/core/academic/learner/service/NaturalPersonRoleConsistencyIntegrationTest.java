package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRole;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.FindNaturalPersonRolesService;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@DisplayName("NaturalPersonRole - Integração")
class NaturalPersonRoleConsistencyIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private LearnerRepository learnerRepository;

    @Autowired
    private ResponsibleRepository responsibleRepository;

    @Autowired
    private NaturalPersonRoleRepository naturalPersonRoleRepository;

    @Autowired
    private EntityManager entityManager;

    private FindNaturalPersonRolesService findRolesService;

    @BeforeEach
    void setUp() {
        findRolesService = new FindNaturalPersonRolesService(naturalPersonRoleRepository);
    }

    private void givenRole(UUID naturalPersonId, NaturalPersonRoleType role) {
        naturalPersonRoleRepository.save(NaturalPersonRole.create(naturalPersonId, role));
    }

    private void givenRevokedRole(UUID naturalPersonId, NaturalPersonRoleType role) {
        naturalPersonRoleRepository.revokeByNaturalPersonIdAndRole(naturalPersonId, role, LocalDateTime.now());
    }

    private NaturalPerson givenPerson(String name, String cpf) {
        return naturalPersonRepository.save(NaturalPerson.create(
                name, name.toLowerCase() + "@email.com", null, cpf, null, null, null, null, null));
    }

    private Learner givenLearner(UUID naturalPersonId) {
        return learnerRepository.save(
                Learner.create(naturalPersonId, null, null, false, null, 2026, null, null));
    }

    @Test
    @DisplayName("Deve gravar e reler os papéis na tabela de roles")
    void handle_ShouldPersistAndReloadRoles() {
        NaturalPerson learnerPerson = givenPerson("Daniela Ferreira", "11144477735");
        NaturalPerson responsiblePerson = givenPerson("Breno Ferreira", "11144477736");

        givenRole(learnerPerson.getId(), NaturalPersonRoleType.LEARNER);
        givenRole(responsiblePerson.getId(), NaturalPersonRoleType.RESPONSIBLE);
        entityManager.flush();
        entityManager.clear();

        Map<UUID, List<NaturalPersonRoleType>> roles =
                findRolesService.handle(List.of(learnerPerson.getId(), responsiblePerson.getId()));

        assertEquals(List.of(NaturalPersonRoleType.LEARNER), roles.get(learnerPerson.getId()));
        assertEquals(List.of(NaturalPersonRoleType.RESPONSIBLE), roles.get(responsiblePerson.getId()));
    }

    @Test
    @DisplayName("Deve acumular os dois papéis da mesma pessoa em uma única resposta")
    void handle_ShouldReturnBothRolesForTheSamePerson() {
        NaturalPerson person = givenPerson("Ana Souza", "11144477737");

        givenRole(person.getId(), NaturalPersonRoleType.LEARNER);
        givenRole(person.getId(), NaturalPersonRoleType.RESPONSIBLE);
        entityManager.flush();
        entityManager.clear();

        Map<UUID, List<NaturalPersonRoleType>> roles = findRolesService.handle(List.of(person.getId()));

        assertEquals(List.of(NaturalPersonRoleType.LEARNER, NaturalPersonRoleType.RESPONSIBLE),
                roles.get(person.getId()));
    }

    @Test
    @DisplayName("Não deve duplicar a linha quando o mesmo papel é concedido de novo")
    void grant_ShouldNotDuplicateRoleRow() {
        NaturalPerson person = givenPerson("Carlos Lima", "11144477738");

        givenRole(person.getId(), NaturalPersonRoleType.LEARNER);
        entityManager.flush();
        entityManager.clear();

        // A idempotência é do LearnerRoleSynchronizer; aqui é o banco que impede a segunda linha,
        // e o índice único parcial só é avaliado no flush.
        assertThrows(ConstraintViolationException.class, () -> {
            givenRole(person.getId(), NaturalPersonRoleType.LEARNER);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Deve permitir conceder o papel novamente depois de revogado")
    void grant_ShouldAllowRoleAgainAfterRevoke() {
        NaturalPerson person = givenPerson("Marina Reis", "11144477739");

        givenRole(person.getId(), NaturalPersonRoleType.RESPONSIBLE);
        givenRevokedRole(person.getId(), NaturalPersonRoleType.RESPONSIBLE);
        givenRole(person.getId(), NaturalPersonRoleType.RESPONSIBLE);
        entityManager.flush();
        entityManager.clear();

        List<NaturalPersonRoleRepository.NaturalPersonRoleProjection> projections =
                naturalPersonRoleRepository.findRolesByNaturalPersonIdsIn(List.of(person.getId()));

        assertEquals(1, projections.size());
        assertEquals(NaturalPersonRoleType.RESPONSIBLE, projections.getFirst().getRole());
    }

    @Test
    @DisplayName("Deve esconder o papel revogado mesmo com a linha ainda na tabela")
    void handle_ShouldHideRevokedRole() {
        NaturalPerson person = givenPerson("Bruno Alves", "11144477740");

        givenRole(person.getId(), NaturalPersonRoleType.LEARNER);
        givenRevokedRole(person.getId(), NaturalPersonRoleType.LEARNER);
        entityManager.flush();
        entityManager.clear();

        assertTrue(findRolesService.handle(List.of(person.getId())).isEmpty());
    }

    @Test
    @DisplayName("Deve revogar os dois papéis de uma vez")
    void revokeAll_ShouldRemoveEveryRoleOfThePerson() {
        NaturalPerson person = givenPerson("Carla Nunes", "11144477741");

        givenRole(person.getId(), NaturalPersonRoleType.LEARNER);
        givenRole(person.getId(), NaturalPersonRoleType.RESPONSIBLE);
        naturalPersonRoleRepository.revokeAllByNaturalPersonId(person.getId(), LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        assertTrue(findRolesService.handle(List.of(person.getId())).isEmpty());
    }

    @Test
    @DisplayName("Deve contar os vínculos de responsável que continuam ativos após a exclusão do educando")
    void count_ShouldIgnoreSoftDeletedResponsibles() {
        NaturalPerson learnerPerson = givenPerson("Diego Ramos", "11144477742");
        NaturalPerson responsiblePerson = givenPerson("Elena Prado", "11144477743");

        Learner learner = givenLearner(learnerPerson.getId());
        responsibleRepository.save(Responsible.create(
                learner, responsiblePerson.getId(), ResponsibleType.FINANCIAL, Kinship.MOTHER, null));
        entityManager.flush();

        assertEquals(1, responsibleRepository.countActiveByNaturalPersonId(responsiblePerson.getId()));

        responsibleRepository.softDeleteByLearnerId(learner.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, responsibleRepository.countActiveByNaturalPersonId(responsiblePerson.getId()));
        assertTrue(responsibleRepository.findNaturalPersonIdsByLearnerId(learner.getId()).isEmpty());
    }

    @Test
    @DisplayName("Deve manter o papel quando o responsável continua vinculado a outro educando")
    void count_ShouldRemainActiveForAnotherLearner() {
        NaturalPerson firstLearnerPerson = givenPerson("Felipe Braga", "11144477744");
        NaturalPerson secondLearnerPerson = givenPerson("Gabi Melo", "11144477745");
        NaturalPerson responsiblePerson = givenPerson("Heitor Vieira", "11144477746");

        Learner firstLearner = givenLearner(firstLearnerPerson.getId());
        Learner secondLearner = givenLearner(secondLearnerPerson.getId());

        responsibleRepository.save(Responsible.create(
                firstLearner, responsiblePerson.getId(), ResponsibleType.FINANCIAL, Kinship.MOTHER, null));
        responsibleRepository.save(Responsible.create(
                secondLearner, responsiblePerson.getId(), ResponsibleType.FINANCIAL, Kinship.MOTHER, null));
        entityManager.flush();

        responsibleRepository.softDeleteByLearnerId(firstLearner.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, responsibleRepository.countActiveByNaturalPersonId(responsiblePerson.getId()));
    }

    @Test
    @DisplayName("Deve devolver vazio para quem não tem nenhum papel")
    void handle_ShouldReturnEmptyWhenPersonHasNoRole() {
        NaturalPerson person = givenPerson("Iris Campos", "11144477747");

        assertTrue(findRolesService.handle(List.of(person.getId())).isEmpty());
    }
}