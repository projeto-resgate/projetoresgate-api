package com.projetoresgate.projetoresgate_api.core.user.domain;

import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserSuccessfully() {
        User user = new User("test@example.com", "password123", "Test User");

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getName());
        assertTrue(user.getRoles().contains(UserRole.USER));
        assertFalse(user.isEmailVerified());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        InternalException exception = assertThrows(InternalException.class, () -> {
            new User("", "password123", "Test User");
        });

        assertEquals("O e-mail não pode ser vazio.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        InternalException exception = assertThrows(InternalException.class, () -> {
            new User("test@example.com", "12345", "Test User");
        });

        assertEquals("A senha deve ter no mínimo 6 caracteres.", exception.getMessage());
    }

    @Test
    void shouldAddRole() {
        User user = new User("test@example.com", "password123", "Test User");
        user.addRole(UserRole.ADMIN);

        assertTrue(user.getRoles().contains(UserRole.ADMIN));
        assertTrue(user.getRoles().contains(UserRole.USER));
    }

    @Test
    void shouldVerifyEmail() {
        User user = new User("test@example.com", "password123", "Test User");
        user.setIsEmailVerified(true);

        assertTrue(user.isEmailVerified());
    }
}
