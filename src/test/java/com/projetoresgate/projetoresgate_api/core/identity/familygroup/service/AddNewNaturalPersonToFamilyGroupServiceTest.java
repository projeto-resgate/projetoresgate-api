package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.AddNewNaturalPersonToFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.CreateNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddNewNaturalPersonToFamilyGroupService - Test")
class AddNewNaturalPersonToFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository familyGroupRepository;

    @Mock
    private CreateNaturalPersonUseCase createNaturalPersonUseCase;

    @InjectMocks
    private AddNewNaturalPersonToFamilyGroupService service;

    private CreateNaturalPersonCommand givenCreateCommand() {
        return new CreateNaturalPersonCommand("Maria Silva", "maria@email.com", null, null,
                null, null, null, null, null);
    }

    @Test
    @DisplayName("Deve criar a pessoa física e vincular ao grupo na mesma operação")
    void handle_ShouldCreateAndLink() {
        UUID groupId = UUID.randomUUID();
        NaturalPerson person = NaturalPerson.create("Maria Silva", "maria@email.com", null, null,
                null, null, null, null, null);
        CreateNaturalPersonCommand createCommand = givenCreateCommand();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(createNaturalPersonUseCase.handle(createCommand)).thenReturn(person);
        when(familyGroupRepository.linkNaturalPerson(groupId, person.getId())).thenReturn(1);

        NaturalPerson result = service.handle(new AddNewNaturalPersonToFamilyGroupCommand(groupId, createCommand));

        assertSame(person, result);
        verify(createNaturalPersonUseCase).handle(createCommand);
        verify(familyGroupRepository).linkNaturalPerson(groupId, person.getId());
    }

    @Test
    @DisplayName("Deve validar o grupo antes de criar, para não enviar e-mail de um cadastro que será desfeito")
    void handle_ShouldValidateFamilyGroupBeforeCreating() {
        UUID groupId = UUID.randomUUID();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + groupId));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(
                new AddNewNaturalPersonToFamilyGroupCommand(groupId, givenCreateCommand())));

        verify(createNaturalPersonUseCase, never()).handle(any());
        verify(familyGroupRepository, never()).linkNaturalPerson(any(), any());
    }

    @Test
    @DisplayName("Deve propagar a falha de criação sem vincular ninguém")
    void handle_ShouldNotLinkWhenCreationFails() {
        UUID groupId = UUID.randomUUID();
        CreateNaturalPersonCommand createCommand = givenCreateCommand();

        when(familyGroupRepository.findByIdOrThrow(groupId))
                .thenReturn(FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, null));
        when(createNaturalPersonUseCase.handle(createCommand))
                .thenThrow(new ResourceNotFoundException("Já existe uma pessoa cadastrada com este CPF."));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new AddNewNaturalPersonToFamilyGroupCommand(groupId, createCommand)));

        verify(familyGroupRepository, never()).linkNaturalPerson(any(), any());
    }

    @Test
    @DisplayName("Deve substituir apenas o id do grupo familiar mantendo os dados da pessoa")
    void withFamilyGroupId_ShouldKeepNaturalPerson() {
        UUID groupId = UUID.randomUUID();
        CreateNaturalPersonCommand createCommand = givenCreateCommand();

        AddNewNaturalPersonToFamilyGroupCommand command =
                new AddNewNaturalPersonToFamilyGroupCommand(null, createCommand).withFamilyGroupId(groupId);

        assertEquals(groupId, command.familyGroupId());
        assertSame(createCommand, command.naturalPerson());
    }
}
