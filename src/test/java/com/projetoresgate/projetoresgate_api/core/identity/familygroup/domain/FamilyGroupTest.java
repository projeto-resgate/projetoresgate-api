package com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FamilyGroup - Entity Test")
class FamilyGroupTest {

    @Test
    @DisplayName("Deve criar grupo familiar com todos os campos")
    void create_ShouldCreateWithAllFields() {
        Address address = Address.create(null, null, "01310-100", "1000", null, "Apto 101", "Bela Vista", "São Paulo", "SP");

        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Silva",
                new BigDecimal("5000.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("800.00"),
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"),
                4,
                address);

        assertNotNull(familyGroup.getId());
        assertEquals("Família Silva", familyGroup.getName());
        assertEquals(new BigDecimal("5000.00"), familyGroup.getHouseholdIncome());
        assertEquals(new BigDecimal("1250.00"), familyGroup.getPerCapitaIncome());
        assertEquals(new BigDecimal("800.00"), familyGroup.getEducationExpense());
        assertEquals(new BigDecimal("400.00"), familyGroup.getHealthExpense());
        assertEquals(new BigDecimal("1500.00"), familyGroup.getHousingExpense());
        assertEquals(4, familyGroup.getNumberOfResidents());
        assertEquals("São Paulo", familyGroup.getAddress().getCity());
        assertTrue(familyGroup.getNaturalPersonList().isEmpty());
    }

    @Test
    @DisplayName("Deve criar grupo familiar sem endereço")
    void create_ShouldCreateWithoutAddress() {
        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Souza", null, null, null, null, null, null, null);

        assertNull(familyGroup.getAddress());
        assertNull(familyGroup.getNumberOfResidents());
        assertNull(familyGroup.getHouseholdIncome());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome for vazio")
    void create_ShouldThrowExceptionWhenNameIsBlank() {
        InternalException exception = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "  ", null, null, null, null, null, null, null));

        assertEquals("O nome do grupo familiar não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome exceder 255 caracteres")
    void create_ShouldThrowExceptionWhenNameIsTooLong() {
        String longName = "a".repeat(256);

        InternalException exception = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", longName, null, null, null, null, null, null, null));

        assertEquals("O nome do grupo familiar não pode exceder 255 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o identificador amigável não seguir o padrão FAM-<número>")
    void create_ShouldThrowExceptionWhenFriendlyIdDoesNotMatchPattern() {
        InternalException missing = assertThrows(InternalException.class, () -> FamilyGroup.create(
                null, "Família Silva", null, null, null, null, null, null, null));

        assertEquals("O identificador amigável do grupo familiar deve seguir o padrão FAM-<número>.",
                missing.getMessage());

        InternalException blank = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "  ", "Família Silva", null, null, null, null, null, null, null));

        assertEquals("O identificador amigável do grupo familiar deve seguir o padrão FAM-<número>.",
                blank.getMessage());

        InternalException wrongPrefix = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "PJ-1", "Família Silva", null, null, null, null, null, null, null));

        assertEquals("O identificador amigável do grupo familiar deve seguir o padrão FAM-<número>.",
                wrongPrefix.getMessage());

        InternalException tooLong = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-123456789012345678", "Família Silva", null, null, null, null, null, null, null));

        assertEquals("O identificador amigável do grupo familiar deve seguir o padrão FAM-<número>.",
                tooLong.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando alguma renda ou despesa for negativa")
    void create_ShouldThrowExceptionWhenFinancialValuesAreNegative() {
        InternalException household = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", new BigDecimal("-1.00"), null, null, null, null, null, null));
        assertEquals("A renda familiar não pode ser negativa.", household.getMessage());

        InternalException perCapita = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", null, new BigDecimal("-1.00"), null, null, null, null, null));
        assertEquals("A renda per capita não pode ser negativa.", perCapita.getMessage());

        InternalException education = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", null, null, new BigDecimal("-1.00"), null, null, null, null));
        assertEquals("A despesa com educação não pode ser negativa.", education.getMessage());

        InternalException health = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", null, null, null, new BigDecimal("-1.00"), null, null, null));
        assertEquals("A despesa com saúde não pode ser negativa.", health.getMessage());

        InternalException housing = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", null, null, null, null, new BigDecimal("-1.00"), null, null));
        assertEquals("A despesa com moradia não pode ser negativa.", housing.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o número de moradores for menor que zero")
    void create_ShouldThrowExceptionWhenNumberOfResidentsIsZero() {
        InternalException exception = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", null, null, null, null, null, 0, null));

        assertEquals("O número de moradores deve ser maior que zero.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o endereço for inválido")
    void create_ShouldThrowExceptionWhenAddressIsInvalid() {
        InternalException exception = assertThrows(InternalException.class, () -> FamilyGroup.create(
                "FAM-1", "Família Silva", null, null, null, null, null, null, Address.create(null, null, "", "1000", null, null, null, "", "")));

        assertEquals("O CEP não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar os campos do grupo familiar")
    void update_ShouldUpdateFields() {
        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Silva", new BigDecimal("5000.00"), null, null, null, null, 4, null);

        FamilyGroup updated = familyGroup.update()
                .name("Família Silva Souza")
                .householdIncome(new BigDecimal("6000.00"))
                .perCapitaIncome(new BigDecimal("1500.00"))
                .educationExpense(new BigDecimal("900.00"))
                .healthExpense(new BigDecimal("500.00"))
                .housingExpense(new BigDecimal("1600.00"))
                .numberOfResidents(5)
                .apply();

        assertEquals("Família Silva Souza", updated.getName());
        assertEquals(new BigDecimal("6000.00"), updated.getHouseholdIncome());
        assertEquals(new BigDecimal("1500.00"), updated.getPerCapitaIncome());
        assertEquals(new BigDecimal("900.00"), updated.getEducationExpense());
        assertEquals(new BigDecimal("500.00"), updated.getHealthExpense());
        assertEquals(new BigDecimal("1600.00"), updated.getHousingExpense());
        assertEquals(5, updated.getNumberOfResidents());
    }

    @Test
    @DisplayName("Deve vincular pessoas físicas ao grupo familiar")
    void update_ShouldLinkNaturalPersons() {
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, null, null);
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", "Maria", "52998224725", "123456789",
                null, null, null, null);

        FamilyGroup updated = familyGroup.update()
                .naturalPersonList(List.of(person))
                .apply();

        assertEquals(1, updated.getNaturalPersonList().size());
        assertEquals("Maria Silva", updated.getNaturalPersonList().get(0).getName());
    }

    @Test
    @DisplayName("Deve retornar lista vazia de pessoas físicas ao definir null")
    void update_ShouldReturnEmptyNaturalPersonListWhenNull() {
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Silva", null, null, null, null, null, null, null);

        FamilyGroup updated = familyGroup.update().naturalPersonList(null).apply();

        assertNotNull(updated.getNaturalPersonList());
        assertTrue(updated.getNaturalPersonList().isEmpty());
    }
}
