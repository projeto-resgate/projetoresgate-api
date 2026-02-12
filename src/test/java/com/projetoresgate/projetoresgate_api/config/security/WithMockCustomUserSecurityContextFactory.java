package com.projetoresgate.projetoresgate_api.config.security;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User principal = new User();
        principal.setId(UUID.fromString(customUser.id()));
        principal.setEmail(customUser.username());
        principal.setName(customUser.name());
        Set<UserRole> roles = Arrays.stream(customUser.roles())
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
        principal.setRoles(roles);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
