package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.service.FindUserService;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.FindUserByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUserService - Test")
class FindUserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private FindUserService service;

    @Test
    @DisplayName("Deve encontrar usuário por ID com sucesso")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = User.create("email@test.com", "encoded", "Name", "nick");
        FindUserByIdQuery query = new FindUserByIdQuery(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        User found = service.handle(query);

        assertNotNull(found);
        assertEquals(user, found);
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado por ID")
    void handle_ShouldThrowExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        FindUserByIdQuery query = new FindUserByIdQuery(id);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.handle(query));
        verify(repository).findById(id);
    }
}
