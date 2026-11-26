package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateClassScheduleService - Test")
class UpdateClassScheduleServiceTest {

    @Mock
    private ClassScheduleRepository repository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private UpdateClassScheduleService service;

    private final ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
            LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());

    private ClassSchedule givenSchedule() {
        ClassSchedule schedule = ClassSchedule.create(classGroup, WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0));
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");
        when(repository.findByIdAndClassGroupIdOrThrow(schedule.getId(), classGroup.getId())).thenReturn(schedule);
        when(repository.save(any(ClassSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(programRepository.findByIdOrThrow(classGroup.getProgramId())).thenReturn(program);
        return schedule;
    }

    @Test
    @DisplayName("Deve alterar o horário e recalcular o título da turma")
    void handle_ShouldUpdateScheduleAndRefreshTitle() {
        ClassSchedule schedule = givenSchedule();
        when(repository.findActiveWeekDaysByClassGroupId(classGroup.getId())).thenReturn(List.of(WeekDay.WEDNESDAY));

        ClassScheduleResponse response = service.handle(new UpdateClassScheduleCommand(schedule.getId(),
                classGroup.getId(), WeekDay.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(16, 0), null));

        assertEquals(WeekDay.WEDNESDAY, response.weekDay());
        assertEquals(LocalTime.of(14, 0), response.startTime());
        assertTrue(response.active());
        assertEquals("Programa A - Qua", classGroup.getTitle());
    }

    @Test
    @DisplayName("Deve desativar o horário quando active for false")
    void handle_ShouldDeactivateSchedule() {
        ClassSchedule schedule = givenSchedule();
        when(repository.findActiveWeekDaysByClassGroupId(classGroup.getId())).thenReturn(List.of());

        ClassScheduleResponse response = service.handle(new UpdateClassScheduleCommand(schedule.getId(),
                classGroup.getId(), WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0), false));

        assertFalse(response.active());
        assertEquals("Programa A", classGroup.getTitle());
    }
}
