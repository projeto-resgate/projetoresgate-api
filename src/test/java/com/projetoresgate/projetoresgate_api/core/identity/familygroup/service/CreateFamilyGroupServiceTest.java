package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroupSequence;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupSequenceRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateFamilyGroupService - Test")
class CreateFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @Mock
    private FamilyGroupSequenceRepository sequenceRepository;

    @InjectMocks
    private CreateFamilyGroupService service;

    private void givenSequence() {
        givenSequence(3L);
    }

    private void givenSequence(Long currentValue) {
        FamilyGroupSequence sequence = FamilyGroupSequence.initial();
        for (long i = 0; i < currentValue; i++) {
            sequence.nextValue();
        }
        lenient().when(sequenceRepository.findByIdForUpdate(eq(FamilyGroupSequence.SINGLETON_ID)))
                .thenReturn(Optional.of(sequence));
    }

    @Test
    @DisplayName("Deve criar grupo familiar com endereço salvo separadamente")
    void handle_ShouldCreateWithAddress() {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Silva",
                new BigDecimal("5000.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("800.00"),
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"),
                4,
                new AddressCommand(null, null, "01310-100", "1000", null, "Apto 101", "Bela Vista", "São Paulo", "SP"));

        givenSequence();
        when(repository.existsByName("Família Silva")).thenReturn(false);
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyGroup result = service.handle(command);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("FAM-4", result.getFriendlyId());
        assertEquals("Família Silva", result.getName());
        assertNotNull(result.getAddress());
        assertEquals("São Paulo", result.getAddress().getCity());
        assertEquals(4, result.getNumberOfResidents());
        assertTrue(result.getNaturalPersonList().isEmpty());
        verify(repository).save(any(FamilyGroup.class));
    }

    @Test
    @DisplayName("Deve criar grupo familiar sem endereço")
    void handle_ShouldCreateWithoutAddress() {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Souza", null, null, null, null, null, null, null);

        givenSequence();
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyGroup result = service.handle(command);

        assertNotNull(result);
        assertNull(result.getAddress());
        assertNull(result.getNumberOfResidents());
    }

    @Test
    @DisplayName("Deve lançar exceção quando já existe grupo familiar com o mesmo nome")
    void handle_ShouldThrowExceptionWhenNameAlreadyExists() {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Silva", null, null, null, null, null, null, null);

        when(repository.existsByName("Família Silva")).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));

        assertEquals("Já existe um grupo familiar cadastrado com este nome.", exception.getMessage());
        verify(repository, never()).save(any(FamilyGroup.class));
    }

    @Test
    @DisplayName("Deve incrementar o identificador amigável a cada criação")
    void handle_ShouldIncrementFriendlyId() {
        CreateFamilyGroupCommand first = new CreateFamilyGroupCommand(
                "Família Alfa", null, null, null, null, null, null, null);
        CreateFamilyGroupCommand second = new CreateFamilyGroupCommand(
                "Família Beta", null, null, null, null, null, null, null);

        givenSequence(9L);
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("FAM-10", service.handle(first).getFriendlyId());
        assertEquals("FAM-11", service.handle(second).getFriendlyId());
    }

    @Test
    @DisplayName("Deve inicializar a sequência quando ela ainda não existir")
    void handle_ShouldCreateSequenceWhenMissing() {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Alfa", null, null, null, null, null, null, null);

        when(sequenceRepository.findByIdForUpdate(eq(FamilyGroupSequence.SINGLETON_ID)))
                .thenReturn(Optional.empty());
        when(sequenceRepository.save(any(FamilyGroupSequence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals("FAM-1", service.handle(command).getFriendlyId());
        verify(sequenceRepository).save(any(FamilyGroupSequence.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o endereço for inválido")
    void handle_ShouldThrowExceptionWhenAddressIsInvalid() {
        givenSequence();
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Silva", null, null, null, null, null, null,
                new AddressCommand(null, null, "", "1000", null, null, null, "São Paulo", "SP"));

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));

        assertEquals("O CEP não pode ser vazio.", exception.getMessage());
        verify(repository, never()).save(any(FamilyGroup.class));
    }
}
