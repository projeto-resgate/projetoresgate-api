package com.projetoresgate.projetoresgate_api.core.user.domain;

import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User - Entity Test")
class UserTest {

    @Test
    @DisplayName("Deve criar usuário com sucesso")
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
    @DisplayName("Deve lançar exceção na validação quando o e-mail for vazio")
    void shouldThrowExceptionWhenEmailIsEmpty() {
        InternalException exception = assertThrows(InternalException.class, () -> {
            User.create("", "password123", "Test User", null);
        });

        assertEquals("O e-mail não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar senha curta pois a validação de tamanho é feita no serviço antes do encoding")
    void shouldAcceptShortPassword() {
        User user = User.create("test@example.com", "12345", "Test User", null);
        assertNotNull(user);
        assertEquals("12345", user.getPassword());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação quando o nome for nulo ou vazio")
    void validate_ShouldFailIfNameIsEmptyOnCreation() {
        InternalException exception = assertThrows(InternalException.class, () -> {
            User.create("test@example.com", "password123", "", null);
        });

        assertEquals("O nome não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o nome for apagado na atualização")
    void validate_ShouldFailIfNameIsBlankedOnUpdate() {
        User user = User.create("test@example.com", "password123", "Test User", "tester");

        InternalException exception = assertThrows(InternalException.class, () -> user.update().name("").nickname("newnick").apply());
        assertEquals("O nome não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar informações do usuário com sucesso via Updater")
    void updateInfo_ShouldUpdateFields() {
        User user = User.create("test@example.com", "password123", "Test User", "tester");
        user.update().name("New Name").nickname("newnick").apply();

        assertEquals("New Name", user.getName());
        assertEquals("newnick", user.getNickname());
        assertDoesNotThrow(user::validate);
    }

    @Test
    @DisplayName("Deve alterar a senha do usuário com sucesso")
    void changePassword_ShouldUpdatePassword() {
        User user = User.create("test@example.com", "password123", "Test User", "tester");
        user.changePassword("newEncodedPassword");

        assertEquals("newEncodedPassword", user.getPassword());
        assertDoesNotThrow(user::validate);
    }

    @Test
    @DisplayName("Deve lançar exceção ao alterar senha para valor vazio")
    void changePassword_ShouldFailIfEmpty() {
        User user = User.create("test@example.com", "password123", "Test User", "tester");

        InternalException exception = assertThrows(InternalException.class, () -> user.changePassword(""));
        assertEquals("A senha não pode ser vazia.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve adicionar role")
    void shouldAddRole() {
        User user = User.create("test@example.com", "password123", "Test User", null);
        user.addRole(UserRole.ADMIN);

        assertTrue(user.getRoles().contains(UserRole.ADMIN));
        assertTrue(user.getRoles().contains(UserRole.USER));
    }

    @Test
    @DisplayName("Deve confirmar e-mail do usuário")
    void shouldVerifyEmail() {
        User user = User.create("test@example.com", "password123", "Test User", null);
        user.confirmEmail();

        assertTrue(user.isEmailVerified());
    }
}