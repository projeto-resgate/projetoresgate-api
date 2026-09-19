package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.EducatorCategoryItemNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.FindEducatorCategoryItemNamesQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindEducatorCategoryItemNamesService - Test")
class FindEducatorCategoryItemNamesServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private EducatorCategoryItemRepository itemRepository;

    @InjectMocks
    private FindEducatorCategoryItemNamesService service;

    @Test
    @DisplayName("Deve retornar nomes das categorias de educador do programa")
    void handle_ShouldReturnItemNames() {
        UUID programId = UUID.randomUUID();
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, null);
        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", program);

        when(programRepository.findByIdOrThrow(programId)).thenReturn(program);
        when(itemRepository.findAll(any(Specification.class))).thenReturn(List.of(item));

        List<EducatorCategoryItemNameResponse> names =
                service.handle(new FindEducatorCategoryItemNamesQuery(programId, "Fono"));

        assertEquals(1, names.size());
        assertEquals("Fonoaudiólogo", names.get(0).name());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o programa não for encontrado")
    void handle_ShouldFailWhenProgramNotFound() {
        UUID programId = UUID.randomUUID();

        when(programRepository.findByIdOrThrow(programId))
                .thenThrow(new ResourceNotFoundException("Programa não encontrado com ID: " + programId));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new FindEducatorCategoryItemNamesQuery(programId, null)));

        verify(itemRepository, never()).findAll(any(Specification.class));
    }
}