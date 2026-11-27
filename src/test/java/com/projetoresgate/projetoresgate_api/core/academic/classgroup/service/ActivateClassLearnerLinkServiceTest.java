package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.ActivateClassLearnerLinkCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivateClassLearnerLinkService - Test")
class ActivateClassLearnerLinkServiceTest {

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private ClassLearnerRepository repository;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private ActivateClassLearnerLinkService service;

    private void givenLearnerWithPerson(UUID learnerId) {
        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        when(learnerRepository.findByIdOrThrow(learnerId)).thenReturn(learner);
        when(naturalPersonRepository.findByIdOrThrow(learner.getNaturalPersonId()))
                .thenReturn(NaturalPerson.create("Ana Souza", "ana@example.com", null, "52998224725",
                        null, null, null, null, null));
    }

    private static ClassGroup givenClassGroup() {
        return ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
    }

    @Test
    @DisplayName("Deve criar o vínculo quando o educando ainda não está na turma")
    void handle_ShouldCreateLinkWhenMissing() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();

        givenLearnerWithPerson(learnerId);
        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(repository.findByClassGroupIdAndLearnerId(classGroup.getId(), learnerId)).thenReturn(Optional.empty());

        service.handle(new ActivateClassLearnerLinkCommand(classGroup.getId(), learnerId));

        ArgumentCaptor<ClassLearner> captor = ArgumentCaptor.forClass(ClassLearner.class);
        verify(repository).save(captor.capture());
        assertEquals(learnerId, captor.getValue().getLearnerId());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    @DisplayName("Deve ativar o vínculo inativo existente em vez de criar outro")
    void handle_ShouldActivateInactiveLink() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();
        ClassLearner link = ClassLearner.create(classGroup, learnerId);
        link.deactivate();

        givenLearnerWithPerson(learnerId);
        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(repository.findByClassGroupIdAndLearnerId(classGroup.getId(), learnerId)).thenReturn(Optional.of(link));

        service.handle(new ActivateClassLearnerLinkCommand(classGroup.getId(), learnerId));

        assertTrue(link.isActive());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Vínculo já ativo não gera erro nem duplicidade")
    void handle_ShouldBeIdempotentWhenAlreadyActive() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();
        ClassLearner link = ClassLearner.create(classGroup, learnerId);

        givenLearnerWithPerson(learnerId);
        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(repository.findByClassGroupIdAndLearnerId(classGroup.getId(), learnerId)).thenReturn(Optional.of(link));

        service.handle(new ActivateClassLearnerLinkCommand(classGroup.getId(), learnerId));

        assertTrue(link.isActive());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando a turma não existe")
    void handle_ShouldFailWhenClassGroupNotFound() {
        UUID classGroupId = UUID.randomUUID();
        when(classGroupRepository.findByIdOrThrow(classGroupId))
                .thenThrow(new ResourceNotFoundException("Turma não encontrada"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new ActivateClassLearnerLinkCommand(classGroupId, UUID.randomUUID())));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve ativar vínculo de educando cuja pessoa física foi excluída")
    void handle_ShouldFailWhenPersonDeleted() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();
        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(learnerRepository.findByIdOrThrow(learnerId)).thenReturn(learner);
        when(naturalPersonRepository.findByIdOrThrow(learner.getNaturalPersonId()))
                .thenThrow(new ResourceNotFoundException("Pessoa física não encontrada"));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new ActivateClassLearnerLinkCommand(classGroup.getId(), learnerId)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve ativar vínculo de educando com situação de desistência")
    void handle_ShouldRejectDroppedOutLearner() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();
        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        learner.dropOut("Mudou de cidade", null);

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(learnerRepository.findByIdOrThrow(learnerId)).thenReturn(learner);

        assertThrows(IllegalStateException.class,
                () -> service.handle(new ActivateClassLearnerLinkCommand(classGroup.getId(), learnerId)));

        verify(repository, never()).save(any(ClassLearner.class));
        verify(repository, never()).findByClassGroupIdAndLearnerId(any(), any());
    }
}
