package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.SoftDeleteUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Serviço de Exclusão Lógica de Usuário - Testes")
class SoftDeleteUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SoftDeleteUserService softDeleteUserService;

    @Test
    @DisplayName("Deve chamar o método de exclusão quando o usuário for encontrado")
    void handle_shouldCallDelete_whenUserIsFound() {
        UUID userId = UUID.randomUUID();
        SoftDeleteUserCommand command = new SoftDeleteUserCommand(userId);
        User existingUser = new User();
        existingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).delete(existingUser);

        softDeleteUserService.handle(command);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(existingUser);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
    void handle_shouldThrowException_whenUserIsNotFound() {
        UUID userId = UUID.randomUUID();
        SoftDeleteUserCommand command = new SoftDeleteUserCommand(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        InternalException exception = assertThrows(InternalException.class, () -> {
            softDeleteUserService.handle(command);
        });

        assertEquals("Usuário não encontrado.", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}
