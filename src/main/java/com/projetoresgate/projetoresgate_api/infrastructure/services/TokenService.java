package com.projetoresgate.projetoresgate_api.infrastructure.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService implements ITokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.access-token.duration:900}")
    private long accessTokenDuration;

    @Value("${api.security.refresh-token.duration:604800}")
    private long refreshTokenDuration;

    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    public String generateAccessToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("projetoresgate_api")
                    .withSubject(user.getId().toString())
                    .withClaim("email", user.getEmail())
                    .withClaim("tokenVersion", user.getTokenVersion())
                    .withExpiresAt(generateAccessTokenExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating access token", exception);
        }
    }

    public String validateToken(String token) throws JWTVerificationException {
        return validateAccessToken(token);
    }

    public String validateAccessToken(String token) throws JWTVerificationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("projetoresgate_api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new InternalException("Token inválido ou expirado.");
        }
    }

    public long getTokenVersion(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer("projetoresgate_api")
                    .build()
                    .verify(token);
            Long tokenVersion = decodedJWT.getClaim("tokenVersion").asLong();
            return tokenVersion == null ? -1L : tokenVersion;
        } catch (JWTVerificationException exception) {
            throw new InternalException("Token inválido ou expirado.");
        }
    }

    private Instant generateAccessTokenExpirationDate() {
        return LocalDateTime.now()
                .plusSeconds(accessTokenDuration)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    public LocalDateTime getRefreshTokenExpiryDate() {
        return LocalDateTime.now().plusSeconds(refreshTokenDuration);
    }

    public long getAccessTokenDurationSeconds() {
        return accessTokenDuration;
    }
}
