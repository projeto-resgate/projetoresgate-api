package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.address.api.dto.AddressResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FamilyGroupResponse(
        UUID id,
        String friendlyId,
        String name,
        BigDecimal householdIncome,
        BigDecimal perCapitaIncome,
        BigDecimal educationExpense,
        BigDecimal healthExpense,
        BigDecimal housingExpense,
        Integer numberOfResidents,
        AddressResponse address,
        LocalDateTime dateCreated
) {
    public static FamilyGroupResponse fromEntity(FamilyGroup entity) {
        return new FamilyGroupResponse(
                entity.getId(),
                entity.getFriendlyId(),
                entity.getName(),
                entity.getHouseholdIncome(),
                entity.getPerCapitaIncome(),
                entity.getEducationExpense(),
                entity.getHealthExpense(),
                entity.getHousingExpense(),
                entity.getNumberOfResidents(),
                AddressResponse.fromEntity(entity.getAddress()),
                entity.getDateCreated()
        );
    }
}
