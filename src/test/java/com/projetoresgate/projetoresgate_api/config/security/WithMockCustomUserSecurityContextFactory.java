package com.projetoresgate.projetoresgate_api.config.security;

import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.infrastructure.security.UserDetailsImpl;
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

        User user = User.create(customUser.username(), "password", customUser.name(), null);
        user.setId(UUID.fromString(customUser.id()));
        Set<UserRole> roles = Arrays.stream(customUser.roles())
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
        user.setRoles(roles);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
