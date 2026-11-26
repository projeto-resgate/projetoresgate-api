package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassGroupCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteClassGroupService - Test")
class SoftDeleteClassGroupServiceTest {

    @Mock
    private ClassGroupRepository repository;

    @Mock
    private ClassScheduleRepository scheduleRepository;

    @InjectMocks
    private SoftDeleteClassGroupService service;

    @Test
    @DisplayName("Deve excluir a turma sem vínculos, junto com os horários, de forma lógica")
    void handle_ShouldSoftDeleteClassGroupAndLinks() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A - 1ª Série", List.of());
        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        ClassGroupRepository.ClassGroupDependencies dependencies = dependencies(0, 0);
        when(repository.countDependencies(classGroup.getId())).thenReturn(dependencies);

        service.handle(new SoftDeleteClassGroupCommand(classGroup.getId()));

        verify(scheduleRepository).softDeleteByClassGroupId(classGroup.getId());
        verify(repository).delete(classGroup);
    }

    @Test
    @DisplayName("Não deve excluir a turma que ainda tem educandos ou educadores vinculados")
    void handle_ShouldBlockWhenClassGroupHasLinks() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A - 1ª Série", List.of());
        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        ClassGroupRepository.ClassGroupDependencies dependencies = dependencies(3, 1);
        when(repository.countDependencies(classGroup.getId())).thenReturn(dependencies);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.handle(new SoftDeleteClassGroupCommand(classGroup.getId())));

        assertEquals("Não é possível excluir a turma porque existem vínculos com: 3 educandos e 1 educador. "
                + "Remova os vínculos antes de excluir.", exception.getMessage());
        verify(repository, never()).delete(any(ClassGroup.class));
        verify(scheduleRepository, never()).softDeleteByClassGroupId(any());
    }

    private static ClassGroupRepository.ClassGroupDependencies dependencies(long learners, long collaborators) {
        ClassGroupRepository.ClassGroupDependencies dependencies = mock(ClassGroupRepository.ClassGroupDependencies.class);
        when(dependencies.getLearners()).thenReturn(learners);
        when(dependencies.getCollaborators()).thenReturn(collaborators);
        return dependencies;
    }
}
