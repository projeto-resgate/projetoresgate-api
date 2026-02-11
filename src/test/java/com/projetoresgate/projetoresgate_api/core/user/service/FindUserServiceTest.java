package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.query.FindUserByIdQuery;
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
    private UserRepository userRepository;

    @InjectMocks
    private FindUserService findUserService;

    @Test
    @DisplayName("Deve retornar o usuário quando encontrado pelo ID")
    void handle_shouldReturnUser_whenFoundById() {

        UUID userId = UUID.randomUUID();
        FindUserByIdQuery query = new FindUserByIdQuery(userId);
        User expectedUser = new User("Test User", "test@example.com", "password");
        expectedUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User foundUser = findUserService.handle(query);

        assertNotNull(foundUser);
        assertEquals(expectedUser.getId(), foundUser.getId());
        assertEquals(expectedUser.getName(), foundUser.getName());
        assertEquals(expectedUser.getEmail(), foundUser.getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o usuário não for encontrado pelo ID")
    void handle_shouldThrowException_whenUserNotFoundById() {
        UUID userId = UUID.randomUUID();
        FindUserByIdQuery query = new FindUserByIdQuery(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            findUserService.handle(query);
        });

        assertEquals("Usuário não encontrado.", exception.getMessage());

        verify(userRepository).findById(userId);
    }
}
