package com.projetoresgate.projetoresgate_api.core.identity.user.service;

import com.projetoresgate.projetoresgate_api.core.identity.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.AuthenticateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.services.IRefreshTokenService;
import com.projetoresgate.projetoresgate_api.infrastructure.services.ITokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ITokenService tokenService;
    private final IRefreshTokenService refreshTokenService;

    public AuthenticateUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ITokenService tokenService, IRefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public AuthenticationResponse handle(AuthenticateUserQuery query) {
        User user = userRepository.findByEmail(query.email())
                .orElse(null);

        if (isNull(user) || !passwordEncoder.matches(query.password(), user.getPassword())) {
            throw new BadCredentialsException("Authentication failed");
        }

        String accessToken = tokenService.generateAccessToken(user);

        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                tokenService.getAccessTokenDurationSeconds(),
                "Bearer",
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRoles()
        );
    }
}
