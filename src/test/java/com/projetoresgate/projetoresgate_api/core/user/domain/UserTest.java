package com.projetoresgate.projetoresgate_api.core.user.domain;

import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserSuccessfully() {
        User user = User.create("test@example.com", "password123", "Test User", "tester");

        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("tester", user.getNickname());
        assertTrue(user.getRoles().contains(UserRole.USER));
        assertFalse(user.isEmailVerified());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        InternalException exception = assertThrows(InternalException.class, () -> {
            User.create("", "password123", "Test User", null);
        });

        assertEquals("O e-mail não pode ser vazio.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        InternalException exception = assertThrows(InternalException.class, () -> {
            User.create("test@example.com", "12345", "Test User", null);
        });

        assertEquals("A senha deve ter no mínimo 6 caracteres.", exception.getMessage());
    }

    @Test
    void shouldAddRole() {
        User user = User.create("test@example.com", "password123", "Test User", null);
        user.addRole(UserRole.ADMIN);

        assertTrue(user.getRoles().contains(UserRole.ADMIN));
        assertTrue(user.getRoles().contains(UserRole.USER));
    }

    @Test
    void shouldVerifyEmail() {
        User user = User.create("test@example.com", "password123", "Test User", null);
        user.setIsEmailVerified(true);

        assertTrue(user.isEmailVerified());
    }
}
