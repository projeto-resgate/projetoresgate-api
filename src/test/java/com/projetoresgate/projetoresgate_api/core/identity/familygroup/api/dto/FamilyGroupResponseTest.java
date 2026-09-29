package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FamilyGroupResponse - DTO Test")
class FamilyGroupResponseTest {

    @Test
    @DisplayName("Deve mapear todos os campos do grupo familiar sem retornar a lista de pessoas físicas")
    void fromEntity_ShouldMapAllFields() {
        Address address = Address.create("Rua", "das Palmeiras", "01310-100", "1000", "Próximo à praça",
                "Apto 101", "Bela Vista", "São Paulo", "SP");
        LocalDateTime dateCreated = LocalDateTime.of(2025, 6, 15, 14, 30, 0);

        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Silva",
                new BigDecimal("5000.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("800.00"),
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"),
                4,
                address);
        familyGroup.setDateCreated(dateCreated);

        FamilyGroupResponse response = FamilyGroupResponse.fromEntity(familyGroup);

        assertNotNull(response);
        assertEquals(familyGroup.getId(), response.id());
        assertEquals(familyGroup.getFriendlyId(), response.friendlyId());
        assertEquals("Família Silva", response.name());
        assertEquals(new BigDecimal("5000.00"), response.householdIncome());
        assertEquals(new BigDecimal("1250.00"), response.perCapitaIncome());
        assertEquals(new BigDecimal("800.00"), response.educationExpense());
        assertEquals(new BigDecimal("400.00"), response.healthExpense());
        assertEquals(new BigDecimal("1500.00"), response.housingExpense());
        assertEquals(4, response.numberOfResidents());
        assertNotNull(response.address());
        assertEquals(address.getId(), response.address().id());
        assertEquals("Rua", response.address().streetType());
        assertEquals("das Palmeiras", response.address().streetName());
        assertEquals("1000", response.address().number());
        assertEquals("Próximo à praça", response.address().referencePoint());
        assertEquals("Apto 101", response.address().complement());
        assertEquals("Bela Vista", response.address().neighborhood());
        assertEquals("São Paulo", response.address().city());
        assertEquals("SP", response.address().state());
        assertEquals("01310-100", response.address().zipCode());
        assertEquals(dateCreated, response.dateCreated());
    }

    @Test
    @DisplayName("Deve retornar address nulo quando o grupo não tiver endereço")
    void fromEntity_ShouldReturnNullAddressWhenNoAddress() {
        FamilyGroup familyGroup = FamilyGroup.create("FAM-1", "Família Souza", null, null, null, null, null, null, null);

        FamilyGroupResponse response = FamilyGroupResponse.fromEntity(familyGroup);

        assertNotNull(response);
        assertNull(response.address());
        assertNull(response.householdIncome());
    }
}
