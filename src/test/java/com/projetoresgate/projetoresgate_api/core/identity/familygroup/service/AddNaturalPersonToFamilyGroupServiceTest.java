package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddNaturalPersonToFamilyGroupService - Test")
class AddNaturalPersonToFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository familyGroupRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private AddNaturalPersonToFamilyGroupService service;

    @Test
    @DisplayName("Deve vincular a pessoa física ao grupo familiar")
    void handle_ShouldLinkNaturalPerson() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(naturalPersonRepository.findByIdOrThrow(personId))
                .thenReturn(NaturalPerson.create("Maria Silva", "maria@email.com", null, null, null,
                        null, null, null, null));
        when(familyGroupRepository.linkNaturalPerson(groupId, personId)).thenReturn(1);

        service.handle(new AddNaturalPersonToFamilyGroupCommand(groupId, personId));

        verify(familyGroupRepository).linkNaturalPerson(groupId, personId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a pessoa já estiver vinculada ao grupo")
    void handle_ShouldFailWhenAlreadyLinked() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(naturalPersonRepository.findByIdOrThrow(personId))
                .thenReturn(NaturalPerson.create("Maria Silva", "maria@email.com", null, null, null,
                        null, null, null, null));
        when(familyGroupRepository.linkNaturalPerson(groupId, personId)).thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.handle(new AddNaturalPersonToFamilyGroupCommand(groupId, personId)));

        assertEquals("Esta pessoa já está vinculada a este grupo familiar.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não for encontrado")
    void handle_ShouldFailWhenFamilyGroupNotFound() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + groupId));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new AddNaturalPersonToFamilyGroupCommand(groupId, personId)));

        verify(naturalPersonRepository, never()).findByIdOrThrow(any());
        verify(familyGroupRepository, never()).linkNaturalPerson(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a pessoa física não for encontrada")
    void handle_ShouldFailWhenNaturalPersonNotFound() {
        UUID groupId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(naturalPersonRepository.findByIdOrThrow(personId))
                .thenThrow(new ResourceNotFoundException("Pessoa não encontrada com ID: " + personId));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new AddNaturalPersonToFamilyGroupCommand(groupId, personId)));

        verify(familyGroupRepository, never()).linkNaturalPerson(any(), any());
    }

    @Test
    @DisplayName("Deve substituir apenas o id do grupo familiar mantendo a pessoa")
    void withFamilyGroupId_ShouldKeepNaturalPerson() {
        UUID personId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();

        AddNaturalPersonToFamilyGroupCommand command =
                new AddNaturalPersonToFamilyGroupCommand(null, personId).withFamilyGroupId(groupId);

        assertEquals(groupId, command.familyGroupId());
        assertEquals(personId, command.naturalPersonId());
    }
}
