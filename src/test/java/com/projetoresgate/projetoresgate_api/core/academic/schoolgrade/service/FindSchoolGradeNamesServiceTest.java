package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeNamesQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindSchoolGradeNamesService - Test")
class FindSchoolGradeNamesServiceTest {

    @Mock
    private SchoolGradeRepository repository;

    @InjectMocks
    private FindSchoolGradeNamesService service;

    @Test
    @DisplayName("Deve converter as séries em respostas enxutas com id, nome e ordem")
    void handle_ShouldMapToNameResponses() {
        SchoolGrade first = SchoolGrade.create("Primeiro ano", 1);
        SchoolGrade second = SchoolGrade.create("Segundo ano", 2);
        when(repository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second)));

        List<SchoolGradeNameResponse> result = service.handle(new FindSchoolGradeNamesQuery("", 10));

        assertEquals(2, result.size());
        assertEquals(first.getId(), result.get(0).id());
        assertEquals("Primeiro ano", result.get(0).name());
        assertEquals(1, result.get(0).gradeOrder());
        assertEquals("Segundo ano", result.get(1).name());
    }

    @Test
    @DisplayName("Deve ordenar pela ordem da série e não pelo nome")
    void handle_ShouldSortByGradeOrder() {
        when(repository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.handle(new FindSchoolGradeNamesQuery("", 10));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(nullable(Specification.class), captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals("gradeOrder", pageable.getSort().getOrderFor("gradeOrder").getProperty());
        assertTrue(pageable.getSort().getOrderFor("gradeOrder").isAscending());
    }

    @Test
    @DisplayName("Deve respeitar o limite informado na query")
    void handle_ShouldRespectLimit() {
        when(repository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.handle(new FindSchoolGradeNamesQuery("", 3));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(nullable(Specification.class), captor.capture());
        assertEquals(3, captor.getValue().getPageSize());
    }

    @Test
    @DisplayName("Deve devolver lista vazia quando não há séries cadastradas")
    void handle_ShouldReturnEmptyList() {
        when(repository.findAll(nullable(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<SchoolGradeNameResponse> result = service.handle(new FindSchoolGradeNamesQuery("zzz", 10));

        assertTrue(result.isEmpty());
    }
}
