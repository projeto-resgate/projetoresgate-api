package com.projetoresgate.projetoresgate_api.core.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.FindNaturalPersonByIdService;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindNaturalPersonByIdService - Test")
class FindNaturalPersonByIdServiceTest {

    @Mock
    private NaturalPersonRepository repository;

    @InjectMocks
    private FindNaturalPersonByIdService service;

    @Test
    @DisplayName("Deve encontrar pessoa física por ID com sucesso")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        NaturalPerson person = NaturalPerson.create("Name", "email@test.com", "nick", "51086174968", null, null, null, null, null);
        FindNaturalPersonByIdQuery query = new FindNaturalPersonByIdQuery(id);

        when(repository.findByIdOrThrow(id)).thenReturn(person);

        NaturalPerson found = service.handle(query);

        assertNotNull(found);
        assertEquals(person, found);
        verify(repository).findByIdOrThrow(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pessoa física não encontrada por ID")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        FindNaturalPersonByIdQuery query = new FindNaturalPersonByIdQuery(id);

        when(repository.findByIdOrThrow(id)).thenThrow(new ResourceNotFoundException("Pessoa física não encontrada."));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(query));
        verify(repository).findByIdOrThrow(id);
    }
}