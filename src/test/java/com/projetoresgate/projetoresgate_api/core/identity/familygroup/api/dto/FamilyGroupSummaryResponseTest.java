package com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FamilyGroupSummaryResponse - DTO Test")
class FamilyGroupSummaryResponseTest {

    @Test
    @DisplayName("Deve montar o resumo apenas com os dados da tela de listagem")
    void fromEntity_ShouldBuildSummaryWithCount() {
        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-7", "Família Silva", null, null, null, null, null, null, null);

        FamilyGroupSummaryResponse response = FamilyGroupSummaryResponse.fromEntity(familyGroup, 3L);

        assertEquals(familyGroup.getId(), response.id());
        assertEquals("FAM-7", response.friendlyId());
        assertEquals("Família Silva", response.name());
        assertEquals(3L, response.registeredPeopleCount());
    }
}
