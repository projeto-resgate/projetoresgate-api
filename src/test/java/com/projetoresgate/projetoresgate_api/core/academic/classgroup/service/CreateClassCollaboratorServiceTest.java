package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
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
@DisplayName("CreateClassCollaboratorService - Test")
class CreateClassCollaboratorServiceTest {

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private ClassCollaboratorRepository repository;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private EducatorCategoryItemRepository educatorCategoryItemRepository;

    @InjectMocks
    private CreateClassCollaboratorService service;

    private ClassGroup givenClassGroup() {
        return ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
    }

    @Test
    @DisplayName("Deve validar categoria no programa e criar o vínculo")
    void handle_ShouldCreateCollaborator() {
        ClassGroup classGroup = givenClassGroup();
        UUID collaboratorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(collaboratorRepository.findByIdOrThrow(collaboratorId)).thenReturn(mock(Collaborator.class));
        when(educatorCategoryItemRepository.findByIdAndProgramIdOrThrow(categoryId, classGroup.getProgramId()))
                .thenReturn(mock(EducatorCategoryItem.class));
        when(repository.existsByClassGroupIdAndCollaboratorId(classGroup.getId(), collaboratorId)).thenReturn(false);
        when(repository.save(any(ClassCollaborator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new CreateClassCollaboratorCommand(classGroup.getId(), collaboratorId, categoryId));

        ArgumentCaptor<ClassCollaborator> captor = ArgumentCaptor.forClass(ClassCollaborator.class);
        verify(repository).save(captor.capture());

        assertEquals(collaboratorId, captor.getValue().getCollaboratorId());
        assertEquals(categoryId, captor.getValue().getEducatorCategoryItemId());
    }

    @Test
    @DisplayName("Não deve permitir vincular o mesmo educador duas vezes")
    void handle_ShouldRejectDuplicateCollaborator() {
        ClassGroup classGroup = givenClassGroup();
        UUID collaboratorId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(collaboratorRepository.findByIdOrThrow(collaboratorId)).thenReturn(mock(Collaborator.class));
        when(educatorCategoryItemRepository.findByIdAndProgramIdOrThrow(categoryId, classGroup.getProgramId()))
                .thenReturn(mock(EducatorCategoryItem.class));
        when(repository.existsByClassGroupIdAndCollaboratorId(classGroup.getId(), collaboratorId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.handle(
                new CreateClassCollaboratorCommand(classGroup.getId(), collaboratorId, categoryId)));

        verify(repository, never()).save(any());
    }
}
