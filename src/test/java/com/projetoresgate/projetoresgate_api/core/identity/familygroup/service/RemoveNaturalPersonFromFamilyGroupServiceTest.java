package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.RemoveNaturalPersonFromFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoveNaturalPersonFromFamilyGroupService - Test")
class RemoveNaturalPersonFromFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository familyGroupRepository;

    @InjectMocks
    private RemoveNaturalPersonFromFamilyGroupService service;

    @Test
    @DisplayName("Deve desvincular a pessoa física do grupo familiar")
    void handle_ShouldUnlinkNaturalPerson() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(familyGroupRepository.unlinkNaturalPerson(groupId, personId)).thenReturn(1);

        service.handle(new RemoveNaturalPersonFromFamilyGroupCommand(groupId, personId));

        verify(familyGroupRepository).unlinkNaturalPerson(groupId, personId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a pessoa não estiver vinculada ao grupo")
    void handle_ShouldFailWhenNotLinked() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(familyGroupRepository.unlinkNaturalPerson(groupId, personId)).thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.handle(new RemoveNaturalPersonFromFamilyGroupCommand(groupId, personId)));

        assertEquals("Esta pessoa não está vinculada a este grupo familiar.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não for encontrado")
    void handle_ShouldFailWhenFamilyGroupNotFound() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + groupId));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new RemoveNaturalPersonFromFamilyGroupCommand(groupId, personId)));

        verify(familyGroupRepository, never()).unlinkNaturalPerson(any(), any());
    }

    @Test
    @DisplayName("Deve substituir apenas o id do grupo familiar mantendo a pessoa")
    void withFamilyGroupId_ShouldKeepNaturalPerson() {
        UUID personId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        RemoveNaturalPersonFromFamilyGroupCommand command =
                new RemoveNaturalPersonFromFamilyGroupCommand(null, personId).withFamilyGroupId(groupId);

        assertEquals(groupId, command.familyGroupId());
        assertEquals(personId, command.naturalPersonId());
    }
}
