package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.SoftDeleteCollaboratorCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteCollaboratorService - Test")
class SoftDeleteCollaboratorServiceTest {

    @Mock
    private CollaboratorRepository repository;

    @InjectMocks
    private SoftDeleteCollaboratorService service;

    @Test
    @DisplayName("Deve realizar o soft delete do colaborador encontrado")
    void handle_ShouldSoftDeleteCollaborator() {
        Collaborator collaborator = Collaborator.create(false, UUID.randomUUID(), null);
        when(repository.findByIdOrThrow(collaborator.getId())).thenReturn(collaborator);

        service.handle(new SoftDeleteCollaboratorCommand(collaborator.getId()));

        verify(repository).delete(collaborator);
    }
}
