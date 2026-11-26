package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindClassGroupByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindClassGroupByIdService - Test")
class FindClassGroupByIdServiceTest {

    @Mock
    private ClassGroupRepository repository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private FindClassGroupByIdService service;

    private final ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
            LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());

    @Test
    @DisplayName("Deve devolver a turma com os nomes do programa e da instituição")
    void handle_ShouldReturnClassGroupWithNames() {
        Program program = mock(Program.class);
        when(program.getName()).thenReturn("Programa A");
        LegalPerson institution = mock(LegalPerson.class);
        when(institution.getName()).thenReturn("Escola X");
        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(programRepository.findById(classGroup.getProgramId())).thenReturn(Optional.of(program));
        when(legalPersonRepository.findById(classGroup.getInstitutionId())).thenReturn(Optional.of(institution));

        ClassGroupResponse response = service.handle(new FindClassGroupByIdQuery(classGroup.getId()));

        assertEquals(classGroup.getId(), response.id());
        assertEquals("Programa A", response.programName());
        assertEquals("Escola X", response.institutionName());
    }

    @Test
    @DisplayName("Deve devolver os nomes nulos quando programa ou instituição não forem encontrados")
    void handle_ShouldReturnNullNamesWhenParentsAreMissing() {
        when(repository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(programRepository.findById(classGroup.getProgramId())).thenReturn(Optional.empty());
        when(legalPersonRepository.findById(classGroup.getInstitutionId())).thenReturn(Optional.empty());

        ClassGroupResponse response = service.handle(new FindClassGroupByIdQuery(classGroup.getId()));

        assertNull(response.programName());
        assertNull(response.institutionName());
    }
}
