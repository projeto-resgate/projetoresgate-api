package com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FamilyGroupSequence - Entity Test")
class FamilyGroupSequenceTest {

    @Test
    @DisplayName("Deve começar em zero na primeira criação")
    void initial_ShouldStartAtZero() {
        FamilyGroupSequence sequence = FamilyGroupSequence.initial();

        assertEquals(FamilyGroupSequence.SINGLETON_ID, sequence.getId());
        assertEquals(0L, sequence.getCurrentValue());
    }

    @Test
    @DisplayName("Deve incrementar e devolver o próximo valor a cada chamada")
    void nextValue_ShouldIncrementSequentially() {
        FamilyGroupSequence sequence = FamilyGroupSequence.initial();

        assertEquals(1L, sequence.nextValue());
        assertEquals(2L, sequence.nextValue());
        assertEquals(3L, sequence.nextValue());
        assertEquals(3L, sequence.getCurrentValue());
    }

    @Test
    @DisplayName("Sequências com o mesmo id devem ser consideradas iguais")
    void equals_ShouldCompareById() {
        FamilyGroupSequence first = FamilyGroupSequence.initial();
        FamilyGroupSequence second = FamilyGroupSequence.initial();

        first.nextValue();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
