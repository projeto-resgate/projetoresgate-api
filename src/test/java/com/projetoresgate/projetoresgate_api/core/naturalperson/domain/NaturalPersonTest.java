package com.projetoresgate.projetoresgate_api.core.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NaturalPerson - Entity Test")
class NaturalPersonTest {

    @Test
    @DisplayName("Deve criar uma pessoa física com sucesso")
    void create_ShouldSucceed() {
        String cpf = "51086174968";
        String rg = "7068613";
        LocalDate birthDate = LocalDate.now().minusYears(20);

        NaturalPerson person = NaturalPerson.create("Test User", "test@test.com", "tester", cpf, rg, birthDate, Gender.MALE, "1199999999", "11988888888");

        assertNotNull(person);
        assertEquals("Test User", person.getName());
        assertEquals("test@test.com", person.getEmail());
        assertEquals("tester", person.getNickname());
        assertEquals(cpf, person.getCpf());
        assertEquals(rg, person.getRg());
        assertEquals(Gender.MALE, person.getGender());
        assertFalse(person.isEmailVerified());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem nome")
    void create_ShouldFailWithoutName() {
        String cpf = "51086174968";
        assertThrows(InternalException.class, () ->
                NaturalPerson.create("", "test@test.com", null, cpf, "1234567", LocalDate.now(), Gender.FEMALE, null, null)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem e-mail")
    void create_ShouldFailWithoutEmail() {
        String cpf = "51086174968";
        assertThrows(InternalException.class, () ->
                NaturalPerson.create("Test User", "", null, cpf, "1234567", LocalDate.now(), Gender.FEMALE, null, null)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o CPF exceder 11 caracteres")
    void validate_ShouldFailIfCpfTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create("Test User", "test@test.com", null, "123456789012345", null, null, null, null, null)
        );
        assertEquals("O CPF não pode exceder 11 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o RG exceder 20 caracteres")
    void validate_ShouldFailIfRgTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create("Test User", "test@test.com", null, "51086174968", "123456789012345678901", null, null, null, null)
        );
        assertEquals("O RG não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se a data de nascimento for no futuro")
    void validate_ShouldFailIfBirthDateInFuture() {
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create("Test User", "test@test.com", null, "51086174968", null, LocalDate.now().plusDays(1), null, null, null)
        );
        assertEquals("A data de nascimento não pode estar no futuro.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o telefone exceder 20 caracteres")
    void validate_ShouldFailIfPhoneTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create("Test User", "test@test.com", null, "51086174968", null, null, null, "123456789012345678901", null)
        );
        assertEquals("O telefone não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o celular exceder 20 caracteres")
    void validate_ShouldFailIfCellphoneTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                NaturalPerson.create("Test User", "test@test.com", null, "51086174968", null, null, null, null, "123456789012345678901")
        );
        assertEquals("O celular não pode exceder 20 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar campos usando o Inner Updater com sucesso")
    void updater_ShouldUpdateFields() {
        NaturalPerson person = NaturalPerson.create("Test User", "test@test.com", "tester", "51086174968", null, null, null, null, null);

        LocalDate newBirthDate = LocalDate.of(1995, 5, 5);

        person.update()
                .name("New Name")
                .email("new@test.com")
                .nickname("newnick")
                .cpf("12345678909")
                .rg("7654321")
                .birthDate(newBirthDate)
                .gender(Gender.FEMALE)
                .phone("2233334444")
                .cellphone("22988887777")
                .apply();

        assertEquals("New Name", person.getName());
        assertEquals("new@test.com", person.getEmail());
        assertEquals("newnick", person.getNickname());
        assertEquals("12345678909", person.getCpf());
        assertEquals("7654321", person.getRg());
        assertEquals(newBirthDate, person.getBirthDate());
        assertEquals(Gender.FEMALE, person.getGender());
        assertEquals("2233334444", person.getPhone());
        assertEquals("22988887777", person.getCellphone());
    }

    @Test
    @DisplayName("Deve confirmar o e-mail da pessoa física")
    void confirmEmail_ShouldSetVerified() {
        NaturalPerson person = NaturalPerson.create("Test User", "test@test.com", null, "51086174968", null, null, null, null, null);

        person.confirmEmail();

        assertTrue(person.isEmailVerified());
    }

    @Test
    @DisplayName("Deve criar uma pessoa física com endereço com sucesso")
    void create_ShouldSucceedWithAddress() {
        Address address = Address.create("01310-100", "1000", "Apto 101", "Bela Vista", "São Paulo", "SP");

        NaturalPerson person = NaturalPerson.create("Test User", "test@test.com", "tester", "51086174968", null, null, Gender.MALE, null, null, address);

        assertNotNull(person);
        assertNotNull(person.getAddress());
        assertEquals("01310-100", person.getAddress().getZipCode());
        assertEquals("São Paulo", person.getAddress().getCity());
    }

    @Test
    @DisplayName("Deve criar uma pessoa física sem endereço com sucesso")
    void create_ShouldSucceedWithoutAddress() {
        NaturalPerson person = NaturalPerson.create("Test User", "test@test.com", "tester", "51086174968", null, null, Gender.MALE, null, null, null);

        assertNotNull(person);
        assertNull(person.getAddress());
    }

    @Test
    @DisplayName("Deve atualizar o endereço usando o Inner Updater")
    void updater_ShouldUpdateAddress() {
        NaturalPerson person = NaturalPerson.create("Test User", "test@test.com", "tester", "51086174968", null, null, null, null, null);

        Address newAddress = Address.create("20040-020", "200", null, "Centro", "Rio de Janeiro", "RJ");

        person.update()
                .address(newAddress)
                .apply();

        assertNotNull(person.getAddress());
        assertEquals("Rio de Janeiro", person.getAddress().getCity());
    }
}
