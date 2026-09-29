package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindSchoolGradeByIdService - Test")
class FindSchoolGradeByIdServiceTest {

    @Mock
    private SchoolGradeRepository repository;

    @InjectMocks
    private FindSchoolGradeByIdService service;

    @Test
    @DisplayName("Deve devolver a série solicitada com nome e ordem")
    void handle_ShouldReturnSchoolGrade() {
        UUID id = UUID.randomUUID();
        SchoolGrade existing = SchoolGrade.create("Primeiro ano do ensino médio", 1);
        when(repository.findByIdOrThrow(id)).thenReturn(existing);

        SchoolGrade result = service.handle(new FindSchoolGradeByIdQuery(id));

        assertEquals(existing.getId(), result.getId());
        assertEquals("Primeiro ano do ensino médio", result.getName());
        assertEquals(1, result.getGradeOrder());
        verify(repository).findByIdOrThrow(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a série não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Série escolar não encontrada com ID: " + id));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new FindSchoolGradeByIdQuery(id)));

        assertEquals("Série escolar não encontrada com ID: " + id, exception.getMessage());
    }
}
