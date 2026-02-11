package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;
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

    private CreateUserCommand createUserCommand;

    @BeforeEach
    void setUp() {
        createUserCommand = new CreateUserCommand(
                "Test User",
                "test@example.com",
                "password123"
        );
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso quando o e-mail não está em uso")
    void handle_shouldCreateUser_whenEmailIsNotInUse() {
        User newUser = new User(
                createUserCommand.email(),
                "encodedPassword",
                createUserCommand.name()
        );
        newUser.setId(UUID.randomUUID());

        when(userRepository.findUserByEmail(createUserCommand.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createUserCommand.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        doNothing().when(requestEmailConfirmationUseCase).handle(createUserCommand.email());

        UUID createdUserId = createUserService.handle(createUserCommand);

        assertNotNull(createdUserId);
        assertEquals(newUser.getId(), createdUserId);

        verify(userRepository).findUserByEmail(createUserCommand.email());
        verify(passwordEncoder).encode(createUserCommand.password());
        verify(userRepository).save(any(User.class));
        verify(requestEmailConfirmationUseCase).handle(createUserCommand.email());
    }

    @Test
    @DisplayName("Deve lançar InternalException quando o e-mail já está em uso")
    void handle_shouldThrowException_whenEmailIsInUse() {
        when(userRepository.findUserByEmail(createUserCommand.email())).thenReturn(Optional.of(new User()));

        InternalException exception = assertThrows(InternalException.class, () -> {
            createUserService.handle(createUserCommand);
        });

        assertEquals("Este e-mail já está cadastrado.", exception.getMessage());

        verify(userRepository).findUserByEmail(createUserCommand.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(requestEmailConfirmationUseCase, never()).handle(anyString());
    }
}
