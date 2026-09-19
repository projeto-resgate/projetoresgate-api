package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindProgramByIdService - Test")
class FindProgramByIdServiceTest {

    @Mock
    private ProgramRepository repository;

    @Mock
    private FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    @InjectMocks
    private FindProgramByIdService service;

    @Test
    @DisplayName("Deve buscar programa por id com instituição resolvida")
    void handle_ShouldReturnProgramWithInstitution() {
        UUID institutionId = UUID.randomUUID();
        Program program = Program.create("Programa A", "https://site.com", ProgramStatus.ACTIVE, institutionId);
        LegalPersonSummaryResponse institution = new LegalPersonSummaryResponse(institutionId, "Escola X", "12345678000195");

        when(repository.findByIdOrThrow(institutionId)).thenReturn(program);
        when(findLegalPersonSummariesUseCase.handle(Set.of(institutionId))).thenReturn(List.of(institution));

        ProgramResponse response = service.handle(new FindProgramByIdQuery(institutionId));

        assertEquals("Programa A", response.name());
        assertNotNull(response.institution());
        assertEquals("Escola X", response.institution().displayName());
    }

    @Test
    @DisplayName("Deve buscar programa sem instituição retornando instituição nula")
    void handle_ShouldReturnProgramWithoutInstitution() {
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, null);
        UUID id = program.getId();

        when(repository.findByIdOrThrow(id)).thenReturn(program);

        ProgramResponse response = service.handle(new FindProgramByIdQuery(id));

        assertEquals("Programa A", response.name());
        assertNull(response.institution());
        verify(findLegalPersonSummariesUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o programa não for encontrado")
    void handle_ShouldFailWhenProgramNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findByIdOrThrow(id)).thenThrow(new ResourceNotFoundException("Programa não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(new FindProgramByIdQuery(id)));
    }
}