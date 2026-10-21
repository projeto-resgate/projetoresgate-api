package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteClassGroupService - Test")
class SoftDeleteClassGroupServiceTest {

    @Mock
    private ClassGroupRepository repository;

    @Mock
    private ClassScheduleRepository scheduleRepository;

    @Mock
    private ClassCollaboratorRepository collaboratorRepository;

    @Mock
    private ClassLearnerRepository learnerRepository;

    @InjectMocks
    private SoftDeleteClassGroupService service;

    @Test
    @DisplayName("Deve excluir a turma e todos os seus vínculos de forma lógica")
    void handle_ShouldSoftDeleteClassGroupAndLinks() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A - 1ª Série", List.of());
        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);

        service.handle(new SoftDeleteClassGroupCommand(classGroup.getId()));

        verify(scheduleRepository).softDeleteByClassGroupId(classGroup.getId());
        verify(collaboratorRepository).softDeleteByClassGroupId(classGroup.getId());
        verify(learnerRepository).softDeleteByClassGroupId(classGroup.getId());
        verify(repository).delete(classGroup);
    }
}
