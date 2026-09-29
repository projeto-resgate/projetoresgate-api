package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindAllSchoolGradesQuery;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindAllSchoolGradesService - Test")
class FindAllSchoolGradesServiceTest {

    @Mock
    private SchoolGradeRepository repository;

    @InjectMocks
    private FindAllSchoolGradesService service;

    @Test
    @DisplayName("Deve converter a página de entidades em página de respostas preservando o total")
    void handle_ShouldMapPagePreservingTotal() {
        Pageable pageable = PageRequest.of(0, 10);
        SchoolGrade first = SchoolGrade.create("Primeiro ano", 1);
        SchoolGrade second = SchoolGrade.create("Segundo ano", 2);
        Page<SchoolGrade> page = new PageImpl<>(List.of(first, second), pageable, 2L);
        when(repository.findAll(nullable(Specification.class), eq(pageable))).thenReturn(page);

        Page<SchoolGradeResponse> result = service.handle(
                new FindAllSchoolGradesQuery(null, null, pageable));

        assertEquals(2, result.getTotalElements());
        assertEquals(pageable, result.getPageable());
        assertEquals("Primeiro ano", result.getContent().get(0).name());
        assertEquals(1, result.getContent().get(0).gradeOrder());
        assertEquals("Segundo ano", result.getContent().get(1).name());
        assertEquals(2, result.getContent().get(1).gradeOrder());
    }

    @Test
    @DisplayName("Deve devolver página vazia preservando o total quando não há séries")
    void handle_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(nullable(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0L));

        Page<SchoolGradeResponse> result = service.handle(
                new FindAllSchoolGradesQuery(null, null, pageable));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve passar o pageable recebido para o repositório sem reordenar")
    void handle_ShouldForwardPageable() {
        Pageable pageable = PageRequest.of(2, 5);
        when(repository.findAll(nullable(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0L));

        service.handle(new FindAllSchoolGradesQuery(null, null, pageable));

        verify(repository).findAll(nullable(Specification.class), eq(pageable));
    }
}
