package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums;

import java.time.DayOfWeek;
import java.util.Optional;

public enum WeekDay {

    MONDAY("Segunda-feira", "Seg", DayOfWeek.MONDAY),
    TUESDAY("Terça-feira", "Ter", DayOfWeek.TUESDAY),
    WEDNESDAY("Quarta-feira", "Qua", DayOfWeek.WEDNESDAY),
    THURSDAY("Quinta-feira", "Qui", DayOfWeek.THURSDAY),
    FRIDAY("Sexta-feira", "Sex", DayOfWeek.FRIDAY),
    SATURDAY("Sábado", "Sáb", DayOfWeek.SATURDAY),
    SUNDAY("Domingo", "Dom", DayOfWeek.SUNDAY);

    private final String label;
    private final String abbreviation;
    private final DayOfWeek dayOfWeek;

    WeekDay(String label, String abbreviation, DayOfWeek dayOfWeek) {
        this.label = label;
        this.abbreviation = abbreviation;
        this.dayOfWeek = dayOfWeek;
    }

    public String getLabel() {
        return label;
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
