package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassScheduleCommand;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteClassScheduleService - Test")
class SoftDeleteClassScheduleServiceTest {

    @Mock
    private ClassScheduleRepository repository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private SoftDeleteClassScheduleService service;

    @Test
    @DisplayName("Deve excluir o horário e atualizar o título da turma")
    void handle_ShouldSoftDeleteAndRefreshTitle() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
        ClassSchedule schedule = ClassSchedule.create(classGroup, WeekDay.MONDAY,
                LocalTime.of(8, 0), LocalTime.of(10, 0));
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");

        when(repository.findByIdAndClassGroupIdOrThrow(schedule.getId(), classGroup.getId()))
                .thenReturn(schedule);
        when(programRepository.findByIdOrThrow(classGroup.getProgramId())).thenReturn(program);
        when(repository.findActiveWeekDaysByClassGroupId(classGroup.getId())).thenReturn(List.of());

        service.handle(new SoftDeleteClassScheduleCommand(classGroup.getId(), schedule.getId()));

        verify(repository).delete(schedule);
        assertEquals("Programa A", classGroup.getTitle());
    }
}
