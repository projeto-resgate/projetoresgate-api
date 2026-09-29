package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindFamilyGroupNaturalPersonsService - Test")
class FindFamilyGroupNaturalPersonsServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @InjectMocks
    private FindFamilyGroupNaturalPersonsService service;

    @Test
    @DisplayName("Deve retornar as pessoas físicas vinculadas ao grupo familiar")
    void handle_ShouldReturnLinkedNaturalPersons() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Ferreira", null, null, null, null, null, 4, null);
        NaturalPerson daniela = NaturalPerson.create(
                "Daniela Ferreira", "daniela@email.com", null, "11144477735", "123456789", null, null, null, "11911112222");
        NaturalPerson breno = NaturalPerson.create(
                "Breno Ferreira", "breno@email.com", null, "11144477736", "223456789", null, null, null, "11911113333");

        familyGroup.update().naturalPersonList(List.of(daniela, breno)).apply();

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);

        List<NaturalPerson> result = service.handle(new FindFamilyGroupNaturalPersonsQuery(id));

        assertEquals(2, result.size());
        assertEquals("Daniela Ferreira", result.getFirst().getName());
        assertEquals("Breno Ferreira", result.get(1).getName());
        verify(repository).findByIdOrThrow(id);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o grupo familiar não tiver pessoas vinculadas")
    void handle_ShouldReturnEmptyListWhenNoLinkedPersons() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Ferreira", null, null, null, null, null, 4, null);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);

        List<NaturalPerson> result = service.handle(new FindFamilyGroupNaturalPersonsQuery(id));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não for encontrado")
    void handle_ShouldThrowExceptionWhenGroupNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new FindFamilyGroupNaturalPersonsQuery(id)));
        verify(repository).findByIdOrThrow(id);
    }
}
