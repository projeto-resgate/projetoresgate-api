package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.LogoutCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutService - Test")
class LogoutServiceTest {

    @Mock
    private IRefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LogoutService service;

    @Test
    @DisplayName("Deve revogar o refresh token e invalidar os access tokens quando o token existe")
    void handle_ShouldRevokeTokenAndInvalidateAccessTokens() {
        User user = User.create("email@test.com", "encoded", "Name", "nick");
        LogoutCommand command = new LogoutCommand("valid-refresh-token");

        when(refreshTokenService.revokeRefreshToken("valid-refresh-token")).thenReturn(Optional.of(user));

        service.handle(command);

        verify(refreshTokenService).revokeRefreshToken("valid-refresh-token");
        verify(userRepository).save(user);
        assertEquals(1L, user.getTokenVersion());
    }

    @Test
    @DisplayName("Não deve invalidar access tokens quando o refresh token não existe")
    void handle_ShouldDoNothingWhenTokenNotFound() {
        LogoutCommand command = new LogoutCommand("unknown-refresh-token");

        when(refreshTokenService.revokeRefreshToken("unknown-refresh-token")).thenReturn(Optional.empty());

        service.handle(command);

        verify(refreshTokenService).revokeRefreshToken("unknown-refresh-token");
        verify(userRepository, never()).save(any());
    }
}
