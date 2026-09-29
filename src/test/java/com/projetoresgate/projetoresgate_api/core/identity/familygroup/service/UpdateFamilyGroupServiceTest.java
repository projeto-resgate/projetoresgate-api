package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateFamilyGroupService - Test")
class UpdateFamilyGroupServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @InjectMocks
    private UpdateFamilyGroupService service;

    @Test
    @DisplayName("Deve atualizar os dados do grupo familiar mantendo o endereço existente")
    void handle_ShouldUpdateKeepingCurrentAddress() {
        UUID id = UUID.randomUUID();
        Address address = Address.create(null, null, "01310-100", "1000", null, "Apto 101", "Bela Vista", "São Paulo", "SP");
        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Silva", new BigDecimal("5000.00"), null, null, null, null, 4, address);

        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                id, "Família Silva Souza", new BigDecimal("6000.00"), new BigDecimal("1500.00"),
                new BigDecimal("900.00"), new BigDecimal("500.00"), new BigDecimal("1600.00"), 5,
                new AddressCommand(null, null, "01310-100", "1000", null, "Apto 202", "Bela Vista", "São Paulo", "SP"));

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyGroup result = service.handle(command);

        assertEquals("Família Silva Souza", result.getName());
        assertEquals(new BigDecimal("6000.00"), result.getHouseholdIncome());
        assertEquals(new BigDecimal("1500.00"), result.getPerCapitaIncome());
        assertEquals(5, result.getNumberOfResidents());
        assertSame(address, result.getAddress());
        assertEquals("Apto 202", result.getAddress().getComplement());
    }

    @Test
    @DisplayName("Deve criar o endereço quando o grupo ainda não possui um")
    void handle_ShouldCreateAddressWhenMissing() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, null, null);

        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                id, "Família Silva", null, null, null, null, null, 3,
                new AddressCommand(null, null, "01310-100", "1000", null, null, "Bela Vista", "São Paulo", "SP"));

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyGroup result = service.handle(command);

        assertNotNull(result.getAddress());
        assertEquals("São Paulo", result.getAddress().getCity());
        assertEquals(3, result.getNumberOfResidents());
    }

    @Test
    @DisplayName("Deve manter o endereço atual quando nenhum endereço for enviado")
    void handle_ShouldKeepAddressWhenCommandAddressIsNull() {
        UUID id = UUID.randomUUID();
        Address address = Address.create(null, null, "01310-100", "1000", null, "Apto 101", "Bela Vista", "São Paulo", "SP");
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, 4, address);

        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                id, "Família Souza", null, null, null, null, null, 2, null);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.save(any(FamilyGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FamilyGroup result = service.handle(command);

        assertEquals("Família Souza", result.getName());
        assertSame(address, result.getAddress());
        assertEquals(2, result.getNumberOfResidents());
    }

    @Test
    @DisplayName("Deve lançar exceção quando já existe outro grupo familiar com o mesmo nome")
    void handle_ShouldThrowExceptionWhenNameAlreadyExists() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, null, null);

        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                id, "Família Souza", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.existsByNameAndIdNot(anyString(), any(UUID.class))).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));

        assertEquals("Já existe um grupo familiar cadastrado com este nome.", exception.getMessage());
        verify(repository, never()).save(any(FamilyGroup.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não for encontrado")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                id, "Família Silva", null, null, null, null, null, null, null);

        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(command));
        verify(repository, never()).save(any(FamilyGroup.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando algum valor financeiro for negativo")
    void handle_ShouldThrowExceptionWhenFinancialValueIsNegative() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, null, null);

        UpdateFamilyGroupCommand command = new UpdateFamilyGroupCommand(
                id, "Família Silva", new BigDecimal("-10.00"), null, null, null, null, null, null);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));

        assertEquals("A renda familiar não pode ser negativa.", exception.getMessage());
        verify(repository, never()).save(any(FamilyGroup.class));
    }
}
