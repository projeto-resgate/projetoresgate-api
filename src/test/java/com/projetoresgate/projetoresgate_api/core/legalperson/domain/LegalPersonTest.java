package com.projetoresgate.projetoresgate_api.core.legalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.domain.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LegalPerson - Entity Test")
class LegalPersonTest {

    private Address buildAddress() {
        return Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
    }

    @Test
    @DisplayName("Deve criar uma pessoa jurídica com sucesso")
    void create_ShouldSucceed() {
        String cnpj = "12345678000195";

        LegalPerson person = LegalPerson.create(
                cnpj, "Razão Social LTDA", "Nome Fantasia", "Display Name", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        assertNotNull(person);
        assertEquals(cnpj, person.getCnpj());
        assertEquals("Razão Social LTDA", person.getCorporateName());
        assertEquals("Nome Fantasia", person.getTradeName());
        assertEquals("Display Name", person.getDisplayName());
        assertEquals("6201-5/00", person.getMainCnaeCode());
        assertEquals(RegistrationStatus.ACTIVE, person.getRegistrationStatus());
        assertEquals(CompanyStatus.ACTIVE, person.getCompanyStatus());
        assertNotNull(person.getAddress());
        assertNull(person.getRepresentative());
    }

    @Test
    @DisplayName("Deve criar uma pessoa jurídica com representante com sucesso")
    void create_ShouldSucceedWithRepresentative() {
        Representative representative = Representative.create("John Doe", "11988887777", "1133334444", "john@doe.com");

        LegalPerson person = LegalPerson.create(
                "12345678000195", "Razão Social LTDA", null, null, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress(), representative);

        assertNotNull(person);
        assertNotNull(person.getRepresentative());
        assertEquals("John Doe", person.getRepresentative().getName());
        assertEquals("john@doe.com", person.getRepresentative().getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem CNPJ")
    void create_ShouldFailWithoutCnpj() {
        InternalException exception = assertThrows(InternalException.class, () ->
                LegalPerson.create("", "Razão Social LTDA", null, null, null,
                        RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress())
        );
        assertEquals("O CNPJ não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem razão social")
    void create_ShouldFailWithoutCorporateName() {
        InternalException exception = assertThrows(InternalException.class, () ->
                LegalPerson.create("12345678000195", "", null, null, null,
                        RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress())
        );
        assertEquals("A razão social não pode ser vazia.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção na validação se o CNPJ exceder 14 caracteres")
    void validate_ShouldFailIfCnpjTooLong() {
        InternalException exception = assertThrows(InternalException.class, () ->
                LegalPerson.create("1234567890123456", "Razão Social LTDA", null, null, null,
                        RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress())
        );
        assertEquals("O CNPJ não pode exceder 14 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem status de registro")
    void create_ShouldFailWithoutRegistrationStatus() {
        InternalException exception = assertThrows(InternalException.class, () ->
                LegalPerson.create("12345678000195", "Razão Social LTDA", null, null, null,
                        null, CompanyStatus.ACTIVE, buildAddress())
        );
        assertEquals("O status de registro é obrigatório.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem status da empresa")
    void create_ShouldFailWithoutCompanyStatus() {
        InternalException exception = assertThrows(InternalException.class, () ->
                LegalPerson.create("12345678000195", "Razão Social LTDA", null, null, null,
                        RegistrationStatus.ACTIVE, null, buildAddress())
        );
        assertEquals("O status da empresa é obrigatório.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar sem endereço")
    void create_ShouldFailWithoutAddress() {
        InternalException exception = assertThrows(InternalException.class, () ->
                LegalPerson.create("12345678000195", "Razão Social LTDA", null, null, null,
                        RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, null)
        );
        assertEquals("O endereço é obrigatório.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com endereço sem CEP")
    void create_ShouldFailWithAddressWithoutZipCode() {
        InternalException exception = assertThrows(InternalException.class, () ->
                Address.create("", null, null, null, "São Paulo", "SP")
        );
        assertEquals("O CEP não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar campos usando o Inner Updater com sucesso")
    void updater_ShouldUpdateFields() {
        LegalPerson person = LegalPerson.create(
                "12345678000195", "Razão Social LTDA", null, null, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress());

        person.update()
                .cnpj("98765432000198")
                .corporateName("Nova Razão Social LTDA")
                .tradeName("Novo Nome Fantasia")
                .displayName("Novo Display Name")
                .mainCnaeCode("8599-6/04")
                .registrationStatus(RegistrationStatus.SUSPENDED)
                .companyStatus(CompanyStatus.INACTIVE)
                .address(Address.create("20040-020", "200", null, "Centro", "Rio de Janeiro", "RJ"))
                .representative(Representative.create("Jane Doe", null, null, "jane@doe.com"))
                .apply();

        assertEquals("98765432000198", person.getCnpj());
        assertEquals("Nova Razão Social LTDA", person.getCorporateName());
        assertEquals("Novo Nome Fantasia", person.getTradeName());
        assertEquals("Novo Display Name", person.getDisplayName());
        assertEquals("8599-6/04", person.getMainCnaeCode());
        assertEquals(RegistrationStatus.SUSPENDED, person.getRegistrationStatus());
        assertEquals(CompanyStatus.INACTIVE, person.getCompanyStatus());
        assertEquals("Rio de Janeiro", person.getAddress().getCity());
        assertEquals("Jane Doe", person.getRepresentative().getName());
    }
}
