package com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CreateUserCommand - Test")
class CreateUserCommandTest {

    @Test
    @DisplayName("Deve tirar os espaços das pontas do e-mail")
    void create_ShouldTrimEmail() {
        assertEquals("ana@example.com", new CreateUserCommand("Ana", "  ana@example.com ", null, "senha123").email());
        assertNull(new CreateUserCommand("Ana", null, null, "senha123").email());
    }
}
