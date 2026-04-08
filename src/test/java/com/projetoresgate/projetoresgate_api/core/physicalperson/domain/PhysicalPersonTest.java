package com.projetoresgate.projetoresgate_api.core.physicalperson.domain;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Rg;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PhysicalPerson - Entity Test")
class PhysicalPersonTest {

    @Test
    @DisplayName("Deve criar uma pessoa física com sucesso")
    void create_ShouldSucceed() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        Cpf cpf = new Cpf("51086174968");
        Rg rg = new Rg("7068613");
        LocalDate birthDate = LocalDate.now().minusYears(20);

        PhysicalPerson person = PhysicalPerson.create(user, cpf, rg, birthDate, Gender.MALE, "1199999999", "11988888888");

        assertNotNull(person);
        assertEquals(cpf, person.getCpf());
        assertEquals(user, person.getUser());
        assertEquals(rg, person.getRg());
        assertEquals(Gender.MALE, person.getGender());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem CPF")
    void create_ShouldFailWithoutCpf() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        assertThrows(InternalException.class, () -> 
            PhysicalPerson.create(user, null, new Rg("1234567"), LocalDate.now(), Gender.FEMALE, null, null)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem usuário")
    void create_ShouldFailWithoutUser() {
        Cpf cpf = new Cpf("51086174968");
        assertThrows(InternalException.class, () -> 
            PhysicalPerson.create(null, cpf, new Rg("1234567"), LocalDate.now(), Gender.FEMALE, null, null)
        );
    }

    @Test
    @DisplayName("Deve atualizar campos via setters e validar com sucesso")
    void setters_ShouldUpdateFields() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        PhysicalPerson person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);

        Cpf newCpf = new Cpf("12345678909");
        Rg newRg = new Rg("7654321");
        LocalDate newBirthDate = LocalDate.of(1995, 5, 5);
        
        person.setCpf(newCpf);
        person.setRg(newRg);
        person.setBirthDate(newBirthDate);
        person.setGender(Gender.FEMALE);
        person.setPhone("2233334444");
        person.setCellphone("22988887777");

        assertEquals(newCpf, person.getCpf());
        assertEquals(newRg, person.getRg());
        assertEquals(newBirthDate, person.getBirthDate());
        assertEquals(Gender.FEMALE, person.getGender());
        assertEquals("2233334444", person.getPhone());
        assertEquals("22988887777", person.getCellphone());
        assertDoesNotThrow(person::validate);
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o CPF for nulo")
    void validate_ShouldFailIfCpfIsNull() {
        User user = User.create("test@test.com", "password123", "Test User", "tester");
        PhysicalPerson person = PhysicalPerson.create(user, new Cpf("51086174968"), null, null, null, null, null);
        
        person.setCpf(null);
        
        InternalException exception = assertThrows(InternalException.class, person::validate);
        assertEquals("O CPF é obrigatório.", exception.getMessage());
    }
}
