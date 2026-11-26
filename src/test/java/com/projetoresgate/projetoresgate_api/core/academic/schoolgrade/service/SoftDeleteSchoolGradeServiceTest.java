package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteSchoolGradeService - Test")
class SoftDeleteSchoolGradeServiceTest {

    @Mock
    private SchoolGradeRepository repository;

    @InjectMocks
    private SoftDeleteSchoolGradeService service;

    @Test
    @DisplayName("Deve delegar a exclusão lógica ao repositório")
    void handle_ShouldCallDelete() {
        UUID id = UUID.randomUUID();
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", null, 1);
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.countDependencies(existing.getId()))
                .thenReturn(mock(SchoolGradeRepository.SchoolGradeDependencies.class));

        service.handle(new SoftDeleteSchoolGradeCommand(id));

        verify(repository).delete(existing);
    }

    @Test
    @DisplayName("Não deve excluir a série usada por turmas, educandos ou responsáveis")
    void handle_ShouldBlockWhenSchoolGradeIsInUse() {
        UUID id = UUID.randomUUID();
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", null, 1);
        SchoolGradeRepository.SchoolGradeDependencies dependencies = mock(SchoolGradeRepository.SchoolGradeDependencies.class);
        when(dependencies.getClassGroups()).thenReturn(2L);
        when(dependencies.getLearners()).thenReturn(1L);
        when(dependencies.getResponsibles()).thenReturn(4L);
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.countDependencies(existing.getId())).thenReturn(dependencies);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.handle(new SoftDeleteSchoolGradeCommand(id)));

        assertEquals("Não é possível excluir a série escolar porque existem vínculos com: 2 turmas, 1 educando "
                + "e 4 responsáveis. Remova os vínculos antes de excluir.", exception.getMessage());
        verify(repository, never()).delete(any(SchoolGrade.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando a série não existe e não tentar excluir")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Série escolar não encontrada com ID: " + id));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new SoftDeleteSchoolGradeCommand(id)));

        assertEquals("Série escolar não encontrada com ID: " + id, exception.getMessage());
        verify(repository, never()).delete(any(SchoolGrade.class));
    }
}
