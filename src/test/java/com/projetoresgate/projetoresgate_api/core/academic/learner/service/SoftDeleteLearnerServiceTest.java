package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRoleHolder;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.SoftDeleteLearnerCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteLearnerService - Test")
class SoftDeleteLearnerServiceTest {

    private static final UUID NATURAL_PERSON_ID = UUID.randomUUID();
    private static final UUID RESPONSIBLE_PERSON_ID = UUID.randomUUID();

    private static Learner givenLearner() {
        return Learner.create(NATURAL_PERSON_ID, null, null, false, null, 2026, null, null);
    }

    @Mock
    private LearnerRepository repository;

    @Mock
    private ResponsibleRepository responsibleRepository;

    @Mock
    private LearnerRoleSynchronizer roleSynchronizer;

    @InjectMocks
    private SoftDeleteLearnerService service;

    @Test
    @DisplayName("Deve excluir os responsáveis antes de excluir o educando")
    void handle_ShouldSoftDeleteResponsiblesBeforeLearner() {
        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        service.handle(new SoftDeleteLearnerCommand(learner.getId()));

        InOrder order = inOrder(responsibleRepository, repository);
        order.verify(responsibleRepository).softDeleteByLearnerId(learner.getId());
        order.verify(repository).delete(learner);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o educando não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(new SoftDeleteLearnerCommand(id)));

        verify(responsibleRepository, never()).softDeleteByLearnerId(any());
        verify(repository, never()).delete(any(Learner.class));
    }

    @Test
    @DisplayName("Deve revogar o papel de educando e o de cada responsável")
    void handle_ShouldRevokeLearnerAndResponsibleRoles() {
        Learner learner = givenLearner();
        learner.syncResponsibleList(List.of(
                Responsible.create(learner, RESPONSIBLE_PERSON_ID, ResponsibleType.FINANCIAL, Kinship.MOTHER, null)));

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        service.handle(new SoftDeleteLearnerCommand(learner.getId()));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(roleSynchronizer).revokeEverything(captor.capture());

        Set<NaturalPersonRoleType> roles = captor.getValue().roleAssignments().stream()
                .map(NaturalPersonRoleHolder::role)
                .collect(Collectors.toSet());

        assertEquals(Set.of(NaturalPersonRoleType.LEARNER, NaturalPersonRoleType.RESPONSIBLE), roles);
    }

    @Test
    @DisplayName("Deve revogar os papéis antes de apagar o educando")
    void handle_ShouldRevokeRolesBeforeDeletingLearner() {
        Learner learner = givenLearner();

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        service.handle(new SoftDeleteLearnerCommand(learner.getId()));

        InOrder inOrder = inOrder(roleSynchronizer, repository);
        inOrder.verify(roleSynchronizer).revokeEverything(any());
        inOrder.verify(repository).delete(learner);
    }

    @Test
    @DisplayName("Deve excluir os responsáveis antes de revogar os papéis")
    void handle_ShouldSoftDeleteResponsiblesBeforeRevokingRoles() {
        Learner learner = givenLearner();

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        service.handle(new SoftDeleteLearnerCommand(learner.getId()));

        InOrder inOrder = inOrder(responsibleRepository, roleSynchronizer);
        inOrder.verify(responsibleRepository).softDeleteByLearnerId(learner.getId());
        inOrder.verify(roleSynchronizer).revokeEverything(any());
    }

    @Test
    @DisplayName("Não deve esquecer papéis quando o educando não for encontrado")
    void handle_ShouldNotForgetRolesWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(new SoftDeleteLearnerCommand(id)));

        verify(roleSynchronizer, never()).revokeEverything(any());
        verify(repository, never()).delete(any(Learner.class));
    }
}
