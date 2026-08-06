package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.service.CreateUserService;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserService - Test")
class CreateUserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserService service;

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void handle_ShouldCreateUser() {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "password123");

        when(repository.findByEmail(command.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(command.password())).thenReturn("encoded-password");
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User created = service.handle(command);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("john@test.com", created.getEmail());
        assertEquals("John Doe", created.getName());
        assertEquals("johny", created.getNickname());

        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o e-mail já estiver cadastrado")
    void handle_ShouldFailWhenEmailAlreadyInUse() {
        CreateUserCommand command = new CreateUserCommand("John Doe", "john@test.com", "johny", "password123");
        User existing = User.create("john@test.com", "encoded", "John", null);

        when(repository.findByEmail(command.email())).thenReturn(Optional.of(existing));

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Este e-mail já está cadastrado.", exception.getMessage());

        verify(repository, never()).save(any());
    }
}
