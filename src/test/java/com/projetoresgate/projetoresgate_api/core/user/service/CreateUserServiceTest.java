package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateUserService - Test")
class CreateUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;

    @InjectMocks
    private CreateUserService createUserService;

    @Test
    @DisplayName("Deve criar usuário com senha com sucesso")
    void handle_shouldCreateUserWithPassword_successfully() {
        CreateUserCommand command = new CreateUserCommand("Test User", "test@example.com", "password123");
        when(userRepository.findUserByEmail(command.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(command.password())).thenReturn("encodedPassword");
        
        User savedUser = new User(command.email(), "encodedPassword", command.name());
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        createUserService.handle(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("encodedPassword", capturedUser.getPassword());
        verify(passwordEncoder).encode("password123");
        verify(requestEmailConfirmationUseCase).handle(command.email());
    }

    @Test
    @DisplayName("Deve criar usuário sem senha com sucesso")
    void handle_shouldCreateUserWithoutPassword_successfully() {
        CreateUserCommand command = new CreateUserCommand("Test User", "test@example.com", null);
        when(userRepository.findUserByEmail(command.email())).thenReturn(Optional.empty());

        User savedUser = new User(command.email(), null, command.name());
        savedUser.setId(UUID.randomUUID());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        createUserService.handle(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertNull(capturedUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
        verify(requestEmailConfirmationUseCase).handle(command.email());
    }

    @Test
    @DisplayName("Deve lançar exceção se a senha for inválida (curta)")
    void handle_shouldThrowException_whenPasswordIsInvalid() {
        CreateUserCommand command = new CreateUserCommand("Test User", "test@example.com", "123");
        when(userRepository.findUserByEmail(command.email())).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> {
            createUserService.handle(command);
        });

        assertEquals("A senha deve ter no mínimo 6 caracteres.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar InternalException quando o e-mail já está em uso")
    void handle_shouldThrowException_whenEmailIsInUse() {
        CreateUserCommand command = new CreateUserCommand("Test User", "test@example.com", "password123");
        when(userRepository.findUserByEmail(command.email())).thenReturn(Optional.of(new User()));

        InternalException exception = assertThrows(InternalException.class, () -> {
            createUserService.handle(command);
        });

        assertEquals("Este e-mail já está cadastrado.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }
}
