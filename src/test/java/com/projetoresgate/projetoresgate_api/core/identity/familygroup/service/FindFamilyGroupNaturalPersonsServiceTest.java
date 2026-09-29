package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
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
import org.springframework.data.domain.*;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindFamilyGroupNaturalPersonsService - Test")
class FindFamilyGroupNaturalPersonsServiceTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by("name").ascending());

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

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.findNaturalPersonsByFamilyGroupId(eq(id), eq(PAGEABLE)))
                .thenReturn(new PageImpl<>(List.of(daniela, breno), PAGEABLE, 2));

        Page<FamilyGroupNaturalPersonResponse> result =
                service.handle(new FindFamilyGroupNaturalPersonsQuery(id, PAGEABLE));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Daniela Ferreira", result.getContent().getFirst().name());
        assertEquals("11144477735", result.getContent().getFirst().cpf());
        assertEquals("11911112222", result.getContent().getFirst().cellphone());
        assertEquals("Breno Ferreira", result.getContent().get(1).name());
        assertEquals(PAGEABLE, result.getPageable());

        verify(repository).findByIdOrThrow(id);
        verify(repository).findNaturalPersonsByFamilyGroupId(id, PAGEABLE);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando o grupo familiar não tiver pessoas vinculadas")
    void handle_ShouldReturnEmptyPageWhenNoLinkedPersons() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Ferreira", null, null, null, null, null, 4, null);

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.findNaturalPersonsByFamilyGroupId(eq(id), eq(PAGEABLE)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        Page<FamilyGroupNaturalPersonResponse> result =
                service.handle(new FindFamilyGroupNaturalPersonsQuery(id, PAGEABLE));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o grupo familiar não for encontrado")
    void handle_ShouldThrowExceptionWhenGroupNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new FindFamilyGroupNaturalPersonsQuery(id, PAGEABLE)));

        verify(repository).findByIdOrThrow(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Deve delegar a paginação recebida para o repositório")
    void handle_ShouldPassPageableToRepository() {
        UUID id = UUID.randomUUID();
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Ferreira", null, null, null, null, null, 4, null);
        Pageable customPageable = PageRequest.of(3, 5, Sort.by("name").descending());

        when(repository.findByIdOrThrow(id)).thenReturn(familyGroup);
        when(repository.findNaturalPersonsByFamilyGroupId(eq(id), any()))
                .thenReturn(new PageImpl<>(List.of(), customPageable, 0));

        service.handle(new FindFamilyGroupNaturalPersonsQuery(id, customPageable));

        verify(repository).findNaturalPersonsByFamilyGroupId(id, customPageable);
    }
}
