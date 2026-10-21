package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums;

import java.time.DayOfWeek;
import java.util.Optional;

public enum WeekDay {

    MONDAY("Seg", DayOfWeek.MONDAY),
    TUESDAY("Ter", DayOfWeek.TUESDAY),
    WEDNESDAY("Qua", DayOfWeek.WEDNESDAY),
    THURSDAY("Qui", DayOfWeek.THURSDAY),
    FRIDAY("Sex", DayOfWeek.FRIDAY),
    SATURDAY("Sáb", DayOfWeek.SATURDAY),
    SUNDAY("Dom", DayOfWeek.SUNDAY);

    private final String abbreviation;
    private final DayOfWeek dayOfWeek;

    WeekDay(String abbreviation, DayOfWeek dayOfWeek) {
        this.abbreviation = abbreviation;
        this.dayOfWeek = dayOfWeek;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public static Optional<WeekDay> from(DayOfWeek dayOfWeek) {
        for (WeekDay weekDay : values()) {
            if (weekDay.dayOfWeek == dayOfWeek) {
                return Optional.of(weekDay);
            }
        }
        return Optional.empty();
    }
}
