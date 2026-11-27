package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
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
    @DisplayName("Deve guardar CPF, RG e telefones sem máscara, inclusive ao editar")
    void createAndUpdate_ShouldRemoveMasks() {
        NaturalPerson person = NaturalPerson.create("Ana", "ana@test.com", null, "123.456.789-09", "mg-12.345.678",
                null, null, "(47) 3333-4444", "(47) 99999-0000");

        assertEquals("12345678909", person.getCpf());
        assertEquals("MG12345678", person.getRg());
        assertEquals("4733334444", person.getPhone());
        assertEquals("47999990000", person.getCellphone());

        person.update().rg("123.456.789-09").cellphone("(11) 98888-7777").apply();

        assertEquals("12345678909", person.getRg());
        assertEquals("11988887777", person.getCellphone());
    }

    @Test
    @DisplayName("Deve criar pessoa sem e-mail, que é opcional, gravando o e-mail em branco como nulo")
    void create_ShouldAllowMissingOrBlankEmail() {
        NaturalPerson withoutEmail = NaturalPerson.create("Sem E-mail", null, null, null, null, null, null, null, null);
        NaturalPerson blankEmail = NaturalPerson.create("E-mail em Branco", "   ", null, null, null, null, null, null, null);

        assertNull(withoutEmail.getEmail());
        assertNull(blankEmail.getEmail());
        assertFalse(withoutEmail.isEmailVerified());
    }

    @Test
    @DisplayName("Tirar o e-mail na edição deve desfazer a verificação; manter o mesmo e-mail não")
    void updateEmail_ShouldResetVerificationOnlyWhenEmailChangesOrIsRemoved() {
        NaturalPerson person = NaturalPerson.create("Ana", "ana@example.com", null, null, null, null, null, null, null);
        person.confirmEmail();

        person.update().email("ANA@example.com ").apply();
        assertTrue(person.isEmailVerified());
        assertEquals("ANA@example.com", person.getEmail());

        person.update().email("").apply();
        assertFalse(person.isEmailVerified());
        assertNull(person.getEmail());
    }
}
