package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByUserIdQuery;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindPhysicalPersonByUserIdService - Test")
class FindPhysicalPersonByUserIdServiceTest {

    @Mock
    private PhysicalPersonRepository repository;

    @InjectMocks
    private FindPhysicalPersonByUserIdService service;

    @Test
    @DisplayName("Deve encontrar pessoa física por ID de usuário com sucesso")
    void handle_ShouldFindByUserIdSuccessfully() {
        UUID userId = UUID.randomUUID();
        User user = User.create("email@test.com", "password123", "Name", "nick");
        PhysicalPerson person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);
        person.setId(UUID.randomUUID());
        FindPhysicalPersonByUserIdQuery query = new FindPhysicalPersonByUserIdQuery(userId);

        when(repository.findByUserIdOrThrow(userId)).thenReturn(person);

        PhysicalPerson found = service.handle(query);

        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(person, found);
        verify(repository).findByUserIdOrThrow(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando pessoa física não encontrada por ID de usuário")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID userId = UUID.randomUUID();
        FindPhysicalPersonByUserIdQuery query = new FindPhysicalPersonByUserIdQuery(userId);

        when(repository.findByUserIdOrThrow(userId)).thenThrow(new ResourceNotFoundException("Pessoa física não encontrada."));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(query));
        verify(repository).findByUserIdOrThrow(userId);
    }
}
