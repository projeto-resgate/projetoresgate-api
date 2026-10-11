package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRole;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearnerRoleSynchronizer - Test")
class LearnerRoleSynchronizerTest {

    private static final UUID LEARNER_PERSON_ID = UUID.randomUUID();
    private static final UUID RESPONSIBLE_PERSON_ID = UUID.randomUUID();

    @Mock
    private NaturalPersonRoleRepository roleRepository;
    @Mock
    private ResponsibleRepository responsibleRepository;
    @Mock
    private LearnerRepository learnerRepository;

    private Learner learner;
    private LearnerRoleSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        learner = Learner.create(LEARNER_PERSON_ID, null, null, false, null, 2026, null, null);
        synchronizer = new LearnerRoleSynchronizer(roleRepository, responsibleRepository, learnerRepository);
    }

    @Test
    @DisplayName("Deve gravar o papel do educando e o de cada responsável")
    void declare_ShouldPersistLearnerAndResponsibleRoles() {
        givenNoRoleGranted();

        synchronizer.declare(givenLearnerWithResponsible());

        ArgumentCaptor<NaturalPersonRole> captor = ArgumentCaptor.forClass(NaturalPersonRole.class);
        verify(roleRepository, times(2)).save(captor.capture());

        assertEquals(List.of(
                        new Role(LEARNER_PERSON_ID, NaturalPersonRoleType.LEARNER),
                        new Role(RESPONSIBLE_PERSON_ID, NaturalPersonRoleType.RESPONSIBLE)),
                captor.getAllValues().stream().map(role -> new Role(role.getNaturalPersonId(), role.getRole())).toList());
    }

    @Test
    @DisplayName("Deve ignorar o papel que já está ativo, evitando duplicidade")
    void declare_ShouldSkipRoleAlreadyGranted() {
        givenRoleAlreadyGranted();

        synchronizer.declare(learner);

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve revogar o papel de educando sem olhar os vínculos de responsável")
    void revokeEverything_ShouldAlwaysRevokeLearnerRole() {
        synchronizer.revokeEverything(learner);

        verify(roleRepository).revokeByNaturalPersonIdAndRole(
                eq(LEARNER_PERSON_ID), eq(NaturalPersonRoleType.LEARNER), any());
        verify(responsibleRepository, never()).countActiveByNaturalPersonId(any());
    }

    @Test
    @DisplayName("Deve revogar também o responsável que ainda estava na lista do educando excluído")
    void revokeEverything_ShouldRevokeResponsiblesStillInTheList() {
        givenRemainingLinks(RESPONSIBLE_PERSON_ID, 0L);
        Learner withResponsible = givenLearnerWithResponsible();

        synchronizer.revokeEverything(withResponsible);

        verify(roleRepository).revokeByNaturalPersonIdAndRole(
                eq(RESPONSIBLE_PERSON_ID), eq(NaturalPersonRoleType.RESPONSIBLE), any());
    }

    @Test
    @DisplayName("Deve revogar o papel de responsável quando não sobrou nenhum vínculo ativo")
    void forgetRemoved_ShouldRevokeResponsibleRoleWithoutRemainingLinks() {
        givenRemainingLinks(RESPONSIBLE_PERSON_ID, 0L);

        synchronizer.forgetRemoved(List.of(givenResponsible()));

        verify(roleRepository).revokeByNaturalPersonIdAndRole(
                eq(RESPONSIBLE_PERSON_ID), eq(NaturalPersonRoleType.RESPONSIBLE), any());
    }

    @Test
    @DisplayName("Deve manter o papel de responsável quando a pessoa ainda responde por outro educando")
    void forgetRemoved_ShouldKeepResponsibleRoleWhenStillLinkedToAnotherLearner() {
        givenRemainingLinks(RESPONSIBLE_PERSON_ID, 1L);

        synchronizer.forgetRemoved(List.of(givenResponsible()));

        verify(roleRepository, never()).revokeByNaturalPersonIdAndRole(
                eq(RESPONSIBLE_PERSON_ID), any(), any());
    }

    @Test
    @DisplayName("Deve garantir o flush antes de contar os vínculos que sobraram")
    void forgetRemoved_ShouldFlushBeforeCountingRemainingLinks() {
        givenRemainingLinks(RESPONSIBLE_PERSON_ID, 0L);

        synchronizer.forgetRemoved(List.of(givenResponsible()));

        InOrder inOrder = inOrder(learnerRepository, responsibleRepository, roleRepository);
        inOrder.verify(learnerRepository).flush();
        inOrder.verify(responsibleRepository).countActiveByNaturalPersonId(RESPONSIBLE_PERSON_ID);
        inOrder.verify(roleRepository).revokeByNaturalPersonIdAndRole(any(), any(), any());
    }

    @Test
    @DisplayName("Deve revogar o papel do responsável que saiu da lista e ficou sem nenhum vínculo")
    void forgetRemoved_ShouldRevokeRoleOfRemovedResponsible() {
        givenRemainingLinks(RESPONSIBLE_PERSON_ID, 0L);

        synchronizer.forgetRemoved(List.of(givenResponsible()));

        verify(roleRepository).revokeByNaturalPersonIdAndRole(
                eq(RESPONSIBLE_PERSON_ID), eq(NaturalPersonRoleType.RESPONSIBLE), any());
    }

    private Responsible givenResponsible() {
        return Responsible.create(learner, RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null);
    }

    private Learner givenLearnerWithResponsible() {
        Learner withResponsible = Learner.create(LEARNER_PERSON_ID, null, null, false, null, 2026, null, null);
        withResponsible.syncResponsibleList(List.of(
                Responsible.create(withResponsible, RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL,
                        Kinship.MOTHER, null)));

        return withResponsible;
    }

    private void givenNoRoleGranted() {
        when(roleRepository.findByNaturalPersonIdAndRole(any(), any()))
                .thenReturn(Optional.empty());
    }

    private void givenRoleAlreadyGranted() {
        when(roleRepository.findByNaturalPersonIdAndRole(any(), any()))
                .thenReturn(Optional.of(NaturalPersonRole.create(LEARNER_PERSON_ID,
                        NaturalPersonRoleType.LEARNER)));
    }

    private void givenRemainingLinks(UUID naturalPersonId, long count) {
        when(responsibleRepository.countActiveByNaturalPersonId(naturalPersonId)).thenReturn(count);
    }

    private record Role(UUID naturalPersonId, NaturalPersonRoleType role) {
    }
}