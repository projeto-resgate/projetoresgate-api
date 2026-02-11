package com.projetoresgate.projetoresgate_api.core.user.service;

import com.projetoresgate.projetoresgate_api.core.user.api.dto.AuthenticationResponse;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.AuthenticateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.query.AuthenticateUserQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.services.TokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthenticateUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public AuthenticationResponse handle(AuthenticateUserQuery query) {
        User user = userRepository.findUserByEmail(query.email())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(query.password(), user.getPassword())) {
            throw new BadCredentialsException("Authentication failed");
        }

        String token = tokenService.generateToken(user.getEmail());

        return new AuthenticationResponse(
                token,
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRoles(),
                user.isEmailVerified()
        );
    }
}
