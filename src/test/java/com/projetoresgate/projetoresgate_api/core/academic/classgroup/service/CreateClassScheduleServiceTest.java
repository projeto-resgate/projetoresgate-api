package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassScheduleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassScheduleCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateClassScheduleService - Test")
class CreateClassScheduleServiceTest {

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private ClassScheduleRepository scheduleRepository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private CreateClassScheduleService service;

    @Test
    @DisplayName("Deve criar o horário e atualizar o título da turma")
    void handle_ShouldCreateScheduleAndRefreshTitle() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");

        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(scheduleRepository.save(any(ClassSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(programRepository.findByIdOrThrow(classGroup.getProgramId())).thenReturn(program);
        when(scheduleRepository.findActiveWeekDaysByClassGroupId(classGroup.getId()))
                .thenReturn(List.of(WeekDay.MONDAY));

        ClassScheduleResponse response = service.handle(new CreateClassScheduleCommand(
                classGroup.getId(), WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)));

        assertEquals(WeekDay.MONDAY, response.weekDay());
        assertEquals("Segunda-feira", response.weekDayLabel());
        assertEquals(LocalTime.of(8, 0), response.startTime());
        assertEquals("Programa A - Seg", classGroup.getTitle());
    }
}
