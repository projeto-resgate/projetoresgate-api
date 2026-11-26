package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.DayOfWeek;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("WeekDay - Test")
class WeekDayTest {

    @ParameterizedTest
    @EnumSource(DayOfWeek.class)
    @DisplayName("Deve converter cada dia da semana do Java no WeekDay correspondente")
    void from_ShouldReturnMatchingWeekDay(DayOfWeek dayOfWeek) {
        Optional<WeekDay> weekDay = WeekDay.from(dayOfWeek);

        assertTrue(weekDay.isPresent());
        assertEquals(dayOfWeek, weekDay.get().getDayOfWeek());
        assertEquals(dayOfWeek.name(), weekDay.get().name());
    }

    @Test
    @DisplayName("Deve devolver vazio quando o dia da semana for nulo")
    void from_ShouldReturnEmpty_WhenDayOfWeekIsNull() {
        assertTrue(WeekDay.from(null).isEmpty());
    }
}
