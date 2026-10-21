package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateClassGroupService - Test")
class CreateClassGroupServiceTest {

    private static final UUID PROGRAM_ID = UUID.randomUUID();
    private static final UUID INSTITUTION_ID = UUID.randomUUID();
    private static final LocalDateTime START_DATE = LocalDateTime.of(2026, 2, 1, 8, 0);

    @Mock
    private ClassGroupRepository repository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @Mock
    private SchoolGradeRepository schoolGradeRepository;

    @InjectMocks
    private CreateClassGroupService service;

    @Test
    @DisplayName("Deve gerar o título com programa e séries e calcular a data de término")
    void handle_ShouldBuildTitleAndEndDate() {
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");
        when(programRepository.findByIdOrThrow(PROGRAM_ID)).thenReturn(program);
        when(legalPersonRepository.findByIdOrThrow(INSTITUTION_ID)).thenReturn(mock(LegalPerson.class));
        when(schoolGradeRepository.findAllById(any())).thenReturn(
                List.of(SchoolGrade.create("2ª Série", null, 2), SchoolGrade.create("1ª Série", null, 1)));
        when(repository.save(any(ClassGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateClassGroupCommand command = new CreateClassGroupCommand(
                PROGRAM_ID, INSTITUTION_ID, List.of(UUID.randomUUID(), UUID.randomUUID()),
                30, START_DATE, 6, null);

        service.handle(command);

        ArgumentCaptor<ClassGroup> captor = ArgumentCaptor.forClass(ClassGroup.class);
        verify(repository).save(captor.capture());

        ClassGroup saved = captor.getValue();
        assertEquals("Programa A - 1ª Série, 2ª Série", saved.getTitle());
        assertEquals(START_DATE.plusMonths(6), saved.getEndDate());
        assertEquals(ChronoUnit.MONTHS.between(saved.getStartDate(), saved.getEndDate()), 6);
        assertEquals(2, saved.getSchoolGradeList().size());
    }

    @Test
    @DisplayName("Deve propagar erro quando alguma série informada não existir")
    void handle_ShouldFailWhenSchoolGradeNotFound() {
        when(programRepository.findByIdOrThrow(PROGRAM_ID)).thenReturn(mock(Program.class));
        when(legalPersonRepository.findByIdOrThrow(INSTITUTION_ID)).thenReturn(mock(LegalPerson.class));
        when(schoolGradeRepository.findAllById(any())).thenReturn(List.of());

        CreateClassGroupCommand command = new CreateClassGroupCommand(
                PROGRAM_ID, INSTITUTION_ID, List.of(UUID.randomUUID()),
                30, START_DATE, 6, null);

        assertThrows(InternalException.class, () -> service.handle(command));

        verify(repository, never()).save(any());
    }
}
