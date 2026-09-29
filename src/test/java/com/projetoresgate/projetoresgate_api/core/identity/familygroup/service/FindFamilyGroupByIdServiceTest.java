package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupByIdQuery;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindFamilyGroupByIdService - Test")
class FindFamilyGroupByIdServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @InjectMocks
    private FindFamilyGroupByIdService service;

    @Test
    @DisplayName("Deve encontrar grupo familiar por ID com sucesso e retornar todos os campos")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Silva", new BigDecimal("5000.00"), new BigDecimal("1250.00"),
                new BigDecimal("800.00"), new BigDecimal("400.00"), new BigDecimal("1500.00"), 4, null);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);

        FamilyGroup found = service.handle(new FindFamilyGroupByIdQuery(id));

        assertNotNull(found);
        assertEquals("Família Silva", found.getName());
        assertEquals(new BigDecimal("5000.00"), found.getHouseholdIncome());
        assertEquals(4, found.getNumberOfResidents());
        verify(repository).findByIdOrThrow(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não for encontrado por ID")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new FindFamilyGroupByIdQuery(id)));
        verify(repository).findByIdOrThrow(id);
    }
}
