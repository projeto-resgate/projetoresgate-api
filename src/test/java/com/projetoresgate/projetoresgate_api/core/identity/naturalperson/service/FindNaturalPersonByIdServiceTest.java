package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @DisplayName("Deve encontrar pessoa física por ID com sucesso e retornar todos os campos")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        LocalDateTime dateCreated = LocalDateTime.of(2025, 6, 15, 14, 30, 0);

        NaturalPerson person = NaturalPerson.create(
                "João Silva", "joao@test.com", "joaozinho",
                "51086174968", "1234567",
                LocalDate.of(1990, 5, 20), Gender.MALE,
                "1133334444", "11988888888"
        );
        person.setDateCreated(dateCreated);

        FindNaturalPersonByIdQuery query = new FindNaturalPersonByIdQuery(id);
        when(repository.findByIdOrThrow(id)).thenReturn(person);

        NaturalPerson found = service.handle(query);

        assertNotNull(found);
        assertEquals("João Silva", found.getName());
        assertEquals("joao@test.com", found.getEmail());
        assertEquals("joaozinho", found.getNickname());
        assertEquals("51086174968", found.getCpf());
        assertEquals("1234567", found.getRg());
        assertEquals(LocalDate.of(1990, 5, 20), found.getBirthDate());
        assertEquals(Gender.MALE, found.getGender());
        assertEquals("1133334444", found.getPhone());
        assertEquals("11988888888", found.getCellphone());
        assertEquals(dateCreated, found.getDateCreated());
        assertFalse(found.isEmailVerified());
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
