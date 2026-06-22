package com.projetoresgate.projetoresgate_api.infrastructure.services;

import jakarta.servlet.http.HttpServletResponse;

public interface ICookieService {
    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);
    void removeRefreshTokenCookie(HttpServletResponse response);
}
