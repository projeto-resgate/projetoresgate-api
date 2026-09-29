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
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        when(repository.findByIdOrThrow(id)).thenReturn(existing);

        service.handle(new SoftDeleteSchoolGradeCommand(id));

        verify(repository).delete(existing);
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
