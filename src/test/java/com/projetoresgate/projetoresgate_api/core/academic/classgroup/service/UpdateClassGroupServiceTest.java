package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateClassGroupService - Test")
class UpdateClassGroupServiceTest {

    private static final UUID PROGRAM_ID = UUID.randomUUID();
    private static final UUID INSTITUTION_ID = UUID.randomUUID();

    @Mock
    private ClassGroupRepository repository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @Mock
    private SchoolGradeRepository schoolGradeRepository;

    @Mock
    private ClassScheduleRepository scheduleRepository;

    @InjectMocks
    private UpdateClassGroupService service;

    private ClassGroup givenClassGroup() {
        return ClassGroup.create(PROGRAM_ID, INSTITUTION_ID, 30,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
    }

    @Test
    @DisplayName("Deve recalcular o título e salvar as alterações")
    void handle_ShouldRefreshTitleAndSave() {
        ClassGroup classGroup = givenClassGroup();
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");

        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(schoolGradeRepository.findAllById(any())).thenReturn(List.of(SchoolGrade.create("1ª Série", "1ª", 1)));
        when(programRepository.findByIdOrThrow(classGroup.getProgramId())).thenReturn(program);
        when(scheduleRepository.findActiveWeekDaysByClassGroupId(classGroup.getId())).thenReturn(List.of());
        when(repository.save(any(ClassGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new UpdateClassGroupCommand(classGroup.getId(), null, null,
                List.of(UUID.randomUUID()), 40, null, null, "observação", false));

        assertEquals("Programa A - 1ª", classGroup.getTitle());
        verify(repository).save(classGroup);
        assertFalse(classGroup.isActive());
    }

    @Test
    @DisplayName("Não deve validar programa nem instituição quando não informados")
    void handle_ShouldNotValidateProgramNorInstitutionWhenNull() {
        ClassGroup classGroup = givenClassGroup();
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");

        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(programRepository.findByIdOrThrow(classGroup.getProgramId())).thenReturn(program);
        when(scheduleRepository.findActiveWeekDaysByClassGroupId(classGroup.getId())).thenReturn(List.of());
        when(repository.save(any(ClassGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new UpdateClassGroupCommand(classGroup.getId(), null, null,
                null, 50, null, null, null, null));

        assertEquals(PROGRAM_ID, classGroup.getProgramId());
        assertEquals(INSTITUTION_ID, classGroup.getInstitutionId());
        verify(legalPersonRepository, never()).findByIdOrThrow(any());
    }
}
