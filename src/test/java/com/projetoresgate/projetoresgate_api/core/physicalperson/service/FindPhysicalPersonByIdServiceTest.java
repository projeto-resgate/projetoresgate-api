package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByIdQuery;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPhysicalPersonByIdService - Test")
class FindPhysicalPersonByIdServiceTest {

    @Mock
    private PhysicalPersonRepository repository;

    @InjectMocks
    private FindPhysicalPersonByIdService service;

    @Test
    @DisplayName("Deve encontrar pessoa física por ID com sucesso")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = User.create("email@test.com", "password123", "Name", "nick");
        PhysicalPerson person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);
        FindPhysicalPersonByIdQuery query = new FindPhysicalPersonByIdQuery(id);

        when(repository.findByIdOrThrow(id)).thenReturn(person);

        PhysicalPerson found = service.handle(query);

        assertNotNull(found);
        assertEquals(person, found);
        verify(repository).findByIdOrThrow(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pessoa física não encontrada por ID")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        FindPhysicalPersonByIdQuery query = new FindPhysicalPersonByIdQuery(id);

        when(repository.findByIdOrThrow(id)).thenThrow(new ResourceNotFoundException("Pessoa física não encontrada."));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(query));
        verify(repository).findByIdOrThrow(id);
    }
}
