package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutAllCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutAllService - Test")
class LogoutAllServiceTest {

    @Mock
    private IRefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LogoutAllService service;

    @Test
    @DisplayName("Deve revogar todos os refresh tokens e invalidar os access tokens")
    void handle_ShouldRevokeAllTokensAndInvalidateAccessTokens() {
        User user = User.create("email@test.com", "encoded", "Name", "nick");
        LogoutAllCommand command = new LogoutAllCommand(user.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.handle(command);

        verify(refreshTokenService).revokeAllUserTokens(user);
        verify(userRepository).save(user);
        assertEquals(1L, user.getTokenVersion());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não for encontrado")
    void handle_ShouldFailWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        LogoutAllCommand command = new LogoutAllCommand(id);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.handle(command));

        verify(refreshTokenService, never()).revokeAllUserTokens(null);
    }
}
