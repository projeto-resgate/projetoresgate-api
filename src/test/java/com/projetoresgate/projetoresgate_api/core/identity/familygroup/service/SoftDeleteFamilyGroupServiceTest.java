package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.SoftDeleteFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteFamilyGroupService - Test")
class SoftDeleteFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @InjectMocks
    private SoftDeleteFamilyGroupService service;

    @Test
    @DisplayName("Deve excluir logicamente o grupo familiar")
    void handle_ShouldSoftDeleteFamilyGroup() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = mock(FamilyGroup.class);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);

        service.handle(new SoftDeleteFamilyGroupCommand(id));

        verify(repository).delete(familyGroup);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não existir")
    void handle_ShouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new SoftDeleteFamilyGroupCommand(id)));

        assertEquals("Grupo familiar não encontrado com ID: " + id, exception.getMessage());
        verify(repository, never()).delete(any(FamilyGroup.class));
    }
}
