package com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ClassScheduleResponse(
        UUID id,
        UUID classGroupId,
        WeekDay weekDay,
        String weekDayLabel,
        LocalTime startTime,
        LocalTime endTime,
        boolean active,
        LocalDateTime dateCreated
) {
    public static ClassScheduleResponse fromEntity(ClassSchedule entity) {
        return new ClassScheduleResponse(
                entity.getId(),
                entity.getClassGroup().getId(),
                entity.getWeekDay(),
                entity.getWeekDay().getLabel(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.isActive(),
                entity.getDateCreated()
        );
    }
}
