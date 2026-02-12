package com.projetoresgate.projetoresgate_api.config.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "test@example.com";

    String name() default "Test User";

    String[] roles() default { "USER" };

    String id() default "11a33607-683c-48e8-a23a-79a60152313c";
}
