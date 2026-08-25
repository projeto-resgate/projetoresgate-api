package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.UpdateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserService - Test")
class UpdateUserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserService service;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        user = User.create("email@test.com", "encoded-old-password", "Old Name", "oldnick");
        userId = user.getId();
    }

    @Test
    @DisplayName("Deve atualizar nome e nickname do usuário com sucesso")
    void handle_ShouldUpdateNameAndNickname() {
        UpdateUserCommand command = new UpdateUserCommand(userId, "New Name", "newnick", null, null);

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.handle(command);

        assertEquals("New Name", user.getName());
        assertEquals("newnick", user.getNickname());
        assertEquals("email@test.com", user.getEmail());
        assertEquals("encoded-old-password", user.getPassword());
        assertEquals(Set.of(UserRole.USER), user.getRoles());
        assertEquals(0L, user.getTokenVersion());
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve alterar a senha quando a senha atual estiver correta")
    void handle_ShouldChangePasswordWhenCurrentPasswordIsCorrect() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, null, "new-password", "old-password");

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(repository.save(user)).thenReturn(user);

        service.handle(command);

        assertEquals("encoded-new-password", user.getPassword());
        verify(passwordEncoder).encode("new-password");
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção ao alterar senha sem informar a senha atual")
    void handle_ShouldFailWhenChangingPasswordWithoutCurrentPassword() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, null, "new-password", null);

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("A senha atual é obrigatória para alterar a senha.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha atual estiver incorreta")
    void handle_ShouldFailWhenCurrentPasswordIsWrong() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, null, "new-password", "wrong-password");

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-old-password")).thenReturn(false);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("A senha atual está incorreta.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
    void handle_ShouldFailWhenUserNotFound() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, null, null, null);

        when(repository.findById(userId)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(repository, never()).save(any());
    }
}
