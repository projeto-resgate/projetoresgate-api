package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramNamesQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindProgramNamesService - Test")
class FindProgramNamesServiceTest {

    @Mock
    private ProgramRepository repository;

    @InjectMocks
    private FindProgramNamesService service;

    @Test
    @DisplayName("Deve retornar nomes de programas com filtro por nome")
    void handle_ShouldReturnNamesFiltered() {
        Program programA = Program.create("Programa Alpha", null, ProgramStatus.ACTIVE, null);
        Program programB = Program.create("Programa Beta", null, ProgramStatus.INACTIVE, null);

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(programA, programB));

        List<ProgramNameResponse> names = service.handle(new FindProgramNamesQuery("Programa"));

        assertEquals(2, names.size());
        assertEquals("Programa Alpha", names.get(0).name());
        assertEquals("Programa Beta", names.get(1).name());
    }
}