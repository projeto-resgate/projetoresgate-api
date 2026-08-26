package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.FindUserByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
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
    @DisplayName("Deve encontrar usuário por ID com sucesso e retornar todos os campos")
    void handle_ShouldFindByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        LocalDateTime dateCreated = LocalDateTime.of(2025, 4, 10, 9, 15, 0);

        User user = User.create("joao@test.com", "encoded-password", "João Silva", "joaozinho");
        user.setDateCreated(dateCreated);

        FindUserByIdQuery query = new FindUserByIdQuery(id);
        when(repository.findById(id)).thenReturn(Optional.of(user));

        User found = service.handle(query);

        assertNotNull(found);
        assertEquals("joao@test.com", found.getEmail());
        assertEquals("encoded-password", found.getPassword());
        assertEquals("João Silva", found.getName());
        assertEquals("joaozinho", found.getNickname());
        assertEquals(Set.of(UserRole.USER), found.getRoles());
        assertEquals(0L, found.getTokenVersion());
        assertEquals(dateCreated, found.getDateCreated());
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
