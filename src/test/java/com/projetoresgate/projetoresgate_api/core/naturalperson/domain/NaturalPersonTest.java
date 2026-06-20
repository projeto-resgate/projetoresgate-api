package com.projetoresgate.projetoresgate_api.core.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NaturalPerson - Entity Test")
class NaturalPersonTest {

    @Test
    @DisplayName("Deve criar uma pessoa física com sucesso")
    void create_ShouldSucceed() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        String cpf = "51086174968";
        String rg = "7068613";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        NaturalPerson person = NaturalPerson.create(user, cpf, rg, birthDate, Gender.MALE, "1199999999", "11988888888");

        assertNotNull(person);
        assertEquals(cpf, person.getCpf());
        assertEquals(user, person.getUser());
        assertEquals(rg, person.getRg());
        assertEquals(Gender.MALE, person.getGender());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem usuário")
    void create_ShouldFailWithoutUser() {
        String cpf = "51086174968";
        assertThrows(InternalException.class, () ->
                NaturalPerson.create(null, cpf, "1234567", LocalDate.now(), Gender.FEMALE, null, null)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o CPF exceder 11 caracteres")
    void validate_ShouldFailIfCpfTooLong() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create(user, "123456789012345", null, null, null, null, null)
        );
        assertEquals("O CPF não pode exceder 11 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o RG exceder 20 caracteres")
    void validate_ShouldFailIfRgTooLong() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create(user, "51086174968", "123456789012345678901", null, null, null, null)
        );
        assertEquals("O RG não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se a data de nascimento for no futuro")
    void validate_ShouldFailIfBirthDateInFuture() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create(user, "51086174968", null, LocalDate.now().plusDays(1), null, null, null)
        );
        assertEquals("A data de nascimento não pode estar no futuro.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o telefone exceder 20 caracteres")
    void validate_ShouldFailIfPhoneTooLong() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create(user, "51086174968", null, null, null, "123456789012345678901", null)
        );
        assertEquals("O telefone não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o celular exceder 20 caracteres")
    void validate_ShouldFailIfCellphoneTooLong() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create(user, "51086174968", null, null, null, null, "123456789012345678901")
        );
        assertEquals("O celular não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar campos usando o Inner Updater com sucesso")
    void updater_ShouldUpdateFields() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        NaturalPerson person = NaturalPerson.create(user, "51086174968", null, null, null, null, null);

        LocalDate newBirthDate = LocalDate.of(1995, 5, 5);

        person.update()
                .cpf("12345678909")
                .rg("7654321")
                .birthDate(newBirthDate)
                .gender(Gender.FEMALE)
                .phone("2233334444")
                .cellphone("22988887777")
                .apply();

        assertEquals("12345678909", person.getCpf());
        assertEquals("7654321", person.getRg());
        assertEquals(newBirthDate, person.getBirthDate());
        assertEquals(Gender.FEMALE, person.getGender());
        assertEquals("2233334444", person.getPhone());
        assertEquals("22988887777", person.getCellphone());
    }
}