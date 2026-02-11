package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.UpdateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de Atualização de Usuário - Testes")
class UpdateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserService updateUserService;

    private User existingUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        existingUser = new User("test@example.com", "encodedCurrentPassword", "Old Name");
        existingUser.setId(userId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
    void handle_shouldThrowException_whenUserNotFound() {
        UpdateUserCommand command = new UpdateUserCommand(userId, "New Name", null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> updateUserService.handle(command));
        assertEquals("Usuário não encontrado.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar apenas o nome do usuário com sucesso")
    void handle_shouldUpdateOnlyName_successfully() {
        UpdateUserCommand command = new UpdateUserCommand(userId, "New Name", null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        updateUserService.handle(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("New Name", savedUser.getName());
        assertEquals("encodedCurrentPassword", savedUser.getPassword());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar a senha sem a senha atual")
    void handle_shouldThrowException_whenPasswordUpdateWithoutCurrentPassword() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, "newPassword", null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        InternalException exception = assertThrows(InternalException.class, () -> updateUserService.handle(command));
        assertEquals("A senha atual é obrigatória para alterar a senha.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha atual não corresponde")
    void handle_shouldThrowException_whenCurrentPasswordDoesNotMatch() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, "newPassword", "wrongCurrentPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongCurrentPassword", "encodedCurrentPassword")).thenReturn(false);

        InternalException exception = assertThrows(InternalException.class, () -> updateUserService.handle(command));
        assertEquals("A senha atual está incorreta.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar a senha com sucesso quando a senha atual corresponde")
    void handle_shouldUpdatePassword_successfully() {
        UpdateUserCommand command = new UpdateUserCommand(userId, null, "newPassword", "correctCurrentPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctCurrentPassword", "encodedCurrentPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        updateUserService.handle(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        assertEquals("encodedNewPassword", savedUser.getPassword());
        assertEquals("Old Name", savedUser.getName());
    }

    @Test
    @DisplayName("Deve atualizar nome e senha com sucesso")
    void handle_shouldUpdateNameAndPassword_successfully() {
        UpdateUserCommand command = new UpdateUserCommand(userId, "New Name", "newPassword", "correctCurrentPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctCurrentPassword", "encodedCurrentPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        updateUserService.handle(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("New Name", savedUser.getName());
        assertEquals("encodedNewPassword", savedUser.getPassword());
    }
}
