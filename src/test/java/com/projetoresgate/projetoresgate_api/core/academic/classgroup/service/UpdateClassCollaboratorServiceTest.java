package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateClassCollaboratorService - Test")
class UpdateClassCollaboratorServiceTest {

    @Mock
    private ClassCollaboratorRepository repository;

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private EducatorCategoryItemRepository educatorCategoryItemRepository;

    @InjectMocks
    private UpdateClassCollaboratorService service;

    private final ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
            LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());

    private ClassCollaborator givenLink(UUID collaboratorId, UUID categoryId) {
        ClassCollaborator link = ClassCollaborator.create(classGroup, collaboratorId, categoryId);
        when(repository.findByIdAndClassGroupIdOrThrow(link.getId(), classGroup.getId())).thenReturn(link);
        when(repository.save(any(ClassCollaborator.class))).thenAnswer(invocation -> invocation.getArgument(0));
        return link;
    }

    private EducatorCategoryItem givenCategory(UUID categoryId, String name) {
        EducatorCategoryItem category = mock(EducatorCategoryItem.class);
        when(category.getName()).thenReturn(name);
        when(educatorCategoryItemRepository.findById(categoryId)).thenReturn(Optional.of(category));
        return category;
    }

    @Test
    @DisplayName("Deve manter a categoria sem revalidar e devolver nome, CPF e RG do educador")
    void handle_ShouldReturnPersonDocuments_WhenCategoryIsUnchanged() {
        UUID categoryId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, UUID.randomUUID(), null);
        NaturalPerson person = NaturalPerson.create("Ana Souza", "ana@example.com", null, "52998224725",
                "MG98765431", null, null, null, null);
        ClassCollaborator link = givenLink(collaborator.getId(), categoryId);
        givenCategory(categoryId, "Professor");
        when(collaboratorRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
        when(naturalPersonRepository.findById(collaborator.getNaturalPersonId()))
                .thenReturn(Optional.of(person));

        ClassCollaboratorResponse response = service.handle(
                new UpdateClassCollaboratorCommand(link.getId(), classGroup.getId(), categoryId, null));

        assertEquals("Ana Souza", response.collaboratorName());
        assertEquals("52998224725", response.collaboratorCpf());
        assertEquals("MG98765431", response.collaboratorRg());
        assertEquals("Professor", response.educatorCategoryItemName());
        assertTrue(response.active());
        verify(educatorCategoryItemRepository, never()).findByIdAndProgramIdOrThrow(any(), any());
    }

    @Test
    @DisplayName("Deve validar a nova categoria no programa da turma e desativar o vínculo")
    void handle_ShouldValidateNewCategoryAndDeactivate() {
        UUID oldCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, UUID.randomUUID(), null);
        NaturalPerson personWithoutDocuments = NaturalPerson.create("Bruno Lima", "bruno@example.com", null, null,
                null, null, null, null, null);
        ClassCollaborator link = givenLink(collaborator.getId(), oldCategoryId);
        EducatorCategoryItem newCategory = givenCategory(newCategoryId, "Auxiliar");
        when(educatorCategoryItemRepository.findByIdAndProgramIdOrThrow(newCategoryId, classGroup.getProgramId()))
                .thenReturn(newCategory);
        when(collaboratorRepository.findById(collaborator.getId())).thenReturn(Optional.of(collaborator));
        when(naturalPersonRepository.findById(collaborator.getNaturalPersonId()))
                .thenReturn(Optional.of(personWithoutDocuments));

        ClassCollaboratorResponse response = service.handle(
                new UpdateClassCollaboratorCommand(link.getId(), classGroup.getId(), newCategoryId, false));

        assertEquals(newCategoryId, response.educatorCategoryItemId());
        assertEquals("Auxiliar", response.educatorCategoryItemName());
        assertFalse(response.active());
        // RG e CPF são opcionais na pessoa física
        assertNull(response.collaboratorCpf());
        assertNull(response.collaboratorRg());
    }

    @Test
    @DisplayName("Deve devolver nome, CPF e RG nulos quando o colaborador não existe mais")
    void handle_ShouldReturnNullPersonData_WhenCollaboratorIsMissing() {
        UUID categoryId = UUID.randomUUID();
        UUID collaboratorId = UUID.randomUUID();
        ClassCollaborator link = givenLink(collaboratorId, categoryId);
        when(educatorCategoryItemRepository.findById(categoryId)).thenReturn(Optional.empty());
        when(collaboratorRepository.findById(collaboratorId)).thenReturn(Optional.empty());

        ClassCollaboratorResponse response = service.handle(
                new UpdateClassCollaboratorCommand(link.getId(), classGroup.getId(), categoryId, true));

        assertNull(response.collaboratorName());
        assertNull(response.collaboratorCpf());
        assertNull(response.collaboratorRg());
        assertNull(response.educatorCategoryItemName());
        verify(naturalPersonRepository, never()).findById(any());
    }
}
