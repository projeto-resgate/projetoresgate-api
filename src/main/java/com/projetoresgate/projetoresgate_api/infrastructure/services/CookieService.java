package com.projetoresgate.projetoresgate_api.infrastructure.services;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService implements ICookieService {

    @Value("${api.security.refresh-token.duration:604800}")
    private long refreshTokenDuration;

    @Value("${api.cookie.secure:true}")
    private boolean secure;

    @Value("${api.cookie.same-site:Strict}")
    private String sameSite;

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .maxAge(refreshTokenDuration)
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void removeRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}

