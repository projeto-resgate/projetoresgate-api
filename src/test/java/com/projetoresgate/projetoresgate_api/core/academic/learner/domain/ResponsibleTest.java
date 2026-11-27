package com.projetoresgate.projetoresgate_api.core.academic.learner.domain;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Responsible - Entity Test")
class ResponsibleTest {

    private final Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);

    @Test
    @DisplayName("Um responsável pode ter os dois tipos, sem repetição e sempre na mesma ordem")
    void create_ShouldKeepMultipleTypesWithoutDuplicates() {
        Responsible responsible = Responsible.create(learner, UUID.randomUUID(),
                List.of(ResponsibleType.FINANCIAL, ResponsibleType.COMMUNICATION, ResponsibleType.FINANCIAL),
                Kinship.MOTHER, null);

        assertEquals(List.of(ResponsibleType.COMMUNICATION, ResponsibleType.FINANCIAL), responsible.getTypes());
    }

    @Test
    @DisplayName("Deve exigir ao menos um tipo de responsabilidade, ignorando valores nulos")
    void create_ShouldRequireAtLeastOneType() {
        UUID personId = UUID.randomUUID();

        assertThrows(InternalException.class,
                () -> Responsible.create(learner, personId, List.of(), Kinship.MOTHER, null));
        assertThrows(InternalException.class,
                () -> Responsible.create(learner, personId, null, Kinship.MOTHER, null));
        assertThrows(InternalException.class,
                () -> Responsible.create(learner, personId, Arrays.asList((ResponsibleType) null), Kinship.MOTHER, null));
    }

    @Test
    @DisplayName("A edição substitui a lista de tipos")
    void updateFrom_ShouldReplaceTypes() {
        UUID personId = UUID.randomUUID();
        Responsible responsible = Responsible.create(learner, personId,
                List.of(ResponsibleType.COMMUNICATION, ResponsibleType.FINANCIAL), Kinship.MOTHER, null);

        responsible.updateFrom(Responsible.create(learner, personId, List.of(ResponsibleType.FINANCIAL), Kinship.MOTHER, null));

        assertEquals(List.of(ResponsibleType.FINANCIAL), responsible.getTypes());
    }
}
