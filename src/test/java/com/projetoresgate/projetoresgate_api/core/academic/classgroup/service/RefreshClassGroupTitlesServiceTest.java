package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.RefreshClassGroupTitlesCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshClassGroupTitlesService - Test")
class RefreshClassGroupTitlesServiceTest {

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private ClassScheduleRepository scheduleRepository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private RefreshClassGroupTitlesService service;

    private Program program(UUID id, String name) {
        Program program = mock(Program.class);
        when(program.getId()).thenReturn(id);
        when(program.getName()).thenReturn(name);
        return program;
    }

    @Test
    @DisplayName("Deve recriar o título das turmas do programa renomeado, com série e dias abreviados")
    void handle_ShouldRefreshTitlesOfProgramClassGroups() {
        UUID programId = UUID.randomUUID();

        ClassGroup classGroup = ClassGroup.create(programId, UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A",
                List.of(SchoolGrade.create("1ª Série", "1ª", 1)));
        Program program = program(programId, "Programa B");
        when(classGroupRepository.findByProgramId(programId)).thenReturn(List.of(classGroup));
        when(programRepository.findAllById(Set.of(programId))).thenReturn(List.of(program));
        when(scheduleRepository.findActiveWeekDaysByClassGroupIds(List.of(classGroup.getId())))
                .thenReturn(List.<Object[]>of(new Object[]{classGroup.getId(), WeekDay.MONDAY}));

        service.handle(RefreshClassGroupTitlesCommand.byProgram(programId));

        assertEquals("Programa B - 1ª - Seg", classGroup.getTitle());
        verify(classGroupRepository).saveAll(List.of(classGroup));
    }

    @Test
    @DisplayName("Deve recriar o título das turmas que usam a série alterada")
    void handle_ShouldRefreshTitlesOfSchoolGradeClassGroups() {
        UUID schoolGradeId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        ClassGroup classGroup = ClassGroup.create(programId, UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A",
                List.of(SchoolGrade.create("1ª Série", "1ª", 1)));
        Program program = program(programId, "Programa A");
        when(classGroupRepository.findBySchoolGradeId(schoolGradeId)).thenReturn(List.of(classGroup));
        when(programRepository.findAllById(Set.of(programId))).thenReturn(List.of(program));
        when(scheduleRepository.findActiveWeekDaysByClassGroupIds(List.of(classGroup.getId())))
                .thenReturn(List.of());

        service.handle(RefreshClassGroupTitlesCommand.bySchoolGrade(schoolGradeId));

        assertEquals("Programa A - 1ª", classGroup.getTitle());
        verify(classGroupRepository).saveAll(List.of(classGroup));
    }

    @Test
    @DisplayName("Não deve consultar nada quando não há turmas afetadas")
    void handle_ShouldDoNothingWhenNoClassGroupAffected() {
        UUID programId = UUID.randomUUID();
        when(classGroupRepository.findByProgramId(programId)).thenReturn(List.of());

        service.handle(RefreshClassGroupTitlesCommand.byProgram(programId));

        verifyNoInteractions(programRepository, scheduleRepository);
        verify(classGroupRepository).findByProgramId(programId);
    }
}
