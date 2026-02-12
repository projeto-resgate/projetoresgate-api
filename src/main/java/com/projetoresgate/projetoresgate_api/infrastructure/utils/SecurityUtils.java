package com.projetoresgate.projetoresgate_api.infrastructure.utils;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Objects.isNull;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isNull(authentication) || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new InternalException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new InternalException("Erro ao obter contexto do usuário.");
    }
}
