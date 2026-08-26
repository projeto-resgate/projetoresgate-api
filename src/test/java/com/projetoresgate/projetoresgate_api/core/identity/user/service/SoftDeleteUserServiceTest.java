package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.SoftDeleteUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteUserService - Test")
class SoftDeleteUserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private SoftDeleteUserService service;

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void handle_ShouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = User.create("email@test.com", "encoded", "Name", "nick");
        SoftDeleteUserCommand command = new SoftDeleteUserCommand(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.handle(command);

        verify(repository).delete(user);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
    void handle_ShouldFailWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        SoftDeleteUserCommand command = new SoftDeleteUserCommand(id);

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(InternalException.class, () -> service.handle(command));

        verify(repository, never()).delete(any());
    }
}
