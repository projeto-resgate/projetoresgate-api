package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.query.SearchProgramQuery;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProgramService - Test")
class SearchProgramServiceTest {

    @Mock
    private ProgramRepository repository;

    @Mock
    private FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    @InjectMocks
    private SearchProgramService service;

    @Test
    @DisplayName("Deve retornar página de programas resolvendo instituições")
    void handle_ShouldReturnPageWithInstitutions() {
        UUID institutionId = UUID.randomUUID();
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, institutionId);
        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", program);
        program.getEducatorCategoryItemList().add(item);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Program> page = new PageImpl<>(List.of(program));
        LegalPersonSummaryResponse institution = new LegalPersonSummaryResponse(institutionId, "Escola X", "12345678000195");

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(findLegalPersonSummariesUseCase.handle(Set.of(institutionId))).thenReturn(List.of(institution));

        Page<ProgramResponse> result = service.handle(new SearchProgramQuery(ProgramStatus.ACTIVE, institutionId, pageable));

        assertEquals(1, result.getTotalElements());
        ProgramResponse response = result.getContent().get(0);
        assertEquals("Programa A", response.name());
        assertEquals("Escola X", response.institution().displayName());
        assertEquals(1, response.educatorCategoryItems().size());
    }

    @Test
    @DisplayName("Deve retornar página vazia sem consultar instituições")
    void handle_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Program> page = new PageImpl<>(List.of());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ProgramResponse> result = service.handle(new SearchProgramQuery(ProgramStatus.ACTIVE, null, pageable));

        assertTrue(result.getContent().isEmpty());
        verify(findLegalPersonSummariesUseCase, never()).handle(any());
    }
}