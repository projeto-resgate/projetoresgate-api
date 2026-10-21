package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateClassLearnerService - Test")
class CreateClassLearnerServiceTest {

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private ClassLearnerRepository repository;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private CreateClassLearnerService service;

    private ClassGroup givenClassGroup() {
        return ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
    }

    @Test
    @DisplayName("Deve vincular o educando à turma")
    void handle_ShouldCreateClassLearner() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(learnerRepository.findByIdOrThrow(learnerId)).thenReturn(mock(Learner.class));
        when(repository.existsByClassGroupIdAndLearnerId(classGroup.getId(), learnerId)).thenReturn(false);
        when(repository.save(any(ClassLearner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new CreateClassLearnerCommand(classGroup.getId(), learnerId));

        ArgumentCaptor<ClassLearner> captor = ArgumentCaptor.forClass(ClassLearner.class);
        verify(repository).save(captor.capture());

        assertEquals(learnerId, captor.getValue().getLearnerId());
        assertEquals(classGroup.getId(), captor.getValue().getClassGroup().getId());
    }

    @Test
    @DisplayName("Não deve permitir vincular o mesmo educando duas vezes")
    void handle_ShouldRejectDuplicateLearner() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(learnerRepository.findByIdOrThrow(learnerId)).thenReturn(mock(Learner.class));
        when(repository.existsByClassGroupIdAndLearnerId(classGroup.getId(), learnerId)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> service.handle(new CreateClassLearnerCommand(classGroup.getId(), learnerId)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve propagar erro quando o educando não existir")
    void handle_ShouldFailWhenLearnerNotFound() {
        ClassGroup classGroup = givenClassGroup();
        UUID learnerId = UUID.randomUUID();

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(learnerRepository.findByIdOrThrow(learnerId))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + learnerId));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new CreateClassLearnerCommand(classGroup.getId(), learnerId)));

        verify(repository, never()).save(any());
    }
}
