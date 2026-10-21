package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindAllClassGroupsService - Test")
class FindAllClassGroupsServiceTest {

    @Mock
    private ClassGroupRepository repository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    @InjectMocks
    private FindAllClassGroupsService service;

    @Test
    @DisplayName("Deve enriquecer o resumo com nome do programa e da instituição")
    void handle_ShouldResolveProgramAndInstitutionNames() {
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, null);
        UUID institutionId = UUID.randomUUID();
        ClassGroup classGroup = ClassGroup.create(program.getId(), institutionId, 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A - 1ª Série", List.of());

        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(classGroup), pageable, 1));
        when(programRepository.findAllById(any())).thenReturn(List.of(program));
        when(findLegalPersonSummariesUseCase.handle(any()))
                .thenReturn(List.of(new LegalPersonSummaryResponse(institutionId, "Escola X", "12345678000199")));

        Page<ClassGroupSummaryResponse> result = service.handle(
                new FindAllClassGroupsQuery(null, null, null, null, pageable));

        assertEquals(1, result.getTotalElements());
        ClassGroupSummaryResponse response = result.getContent().getFirst();
        assertEquals("Programa A", response.programName());
        assertEquals("Escola X", response.institutionName());
        assertEquals("Programa A - 1ª Série", response.title());
    }
}
