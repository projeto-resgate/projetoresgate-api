package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindProgramNamesQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
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

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(programA, programB)));

        List<ProgramNameResponse> names = service.handle(new FindProgramNamesQuery("Programa", 10));

        assertEquals(2, names.size());
        assertEquals(programA.getId(), names.get(0).id());
        assertEquals("Programa Alpha", names.get(0).name());
        assertEquals(programB.getId(), names.get(1).id());
        assertEquals("Programa Beta", names.get(1).name());
    }

    @Test
    @DisplayName("Deve aplicar o limite e a ordenação alfabética na consulta")
    void handle_ShouldLimitAndSortAlphabetically() {
        when(repository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.handle(new FindProgramNamesQuery("", 5));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(nullable(Specification.class), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "name"), pageable.getSort());
    }
}