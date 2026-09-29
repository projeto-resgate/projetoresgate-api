package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSchoolGradeService - Test")
class UpdateSchoolGradeServiceTest {

    @Mock
    private SchoolGradeRepository repository;

    @InjectMocks
    private UpdateSchoolGradeService service;

    @Test
    @DisplayName("Deve atualizar nome e ordem da série existente")
    void handle_ShouldUpdateSchoolGrade() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.existsByNameAndIdNot("Segundo ano", id)).thenReturn(false);
        when(repository.save(any(SchoolGrade.class))).thenAnswer(i -> i.getArgument(0));

        SchoolGrade result = service.handle(new UpdateSchoolGradeCommand(id, "Segundo ano", 2));

        assertEquals("Segundo ano", result.getName());
        assertEquals(2, result.getGradeOrder());
        verify(repository).save(any(SchoolGrade.class));
    }

    @Test
    @DisplayName("Deve manter o mesmo id da série atualizada")
    void handle_ShouldKeepSameId() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.save(any(SchoolGrade.class))).thenAnswer(i -> i.getArgument(0));

        SchoolGrade result = service.handle(new UpdateSchoolGradeCommand(id, "Segundo ano", 2).withId(id));

        assertEquals(existing.getId(), result.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a série não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Série escolar não encontrada com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new UpdateSchoolGradeCommand(id, "Segundo ano", 2)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já pertence a outra série")
    void handle_ShouldFailWhenNameBelongsToAnother() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.existsByNameAndIdNot("Segundo ano", id)).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class,
                () -> service.handle(new UpdateSchoolGradeCommand(id, "Segundo ano", 2)));

        assertEquals("Já existe uma série escolar cadastrada com este nome.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir manter o próprio nome sem acusar duplicidade")
    void handle_ShouldAllowKeepingOwnName() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.existsByNameAndIdNot("Primeiro ano", id)).thenReturn(false);
        when(repository.save(any(SchoolGrade.class))).thenAnswer(i -> i.getArgument(0));

        SchoolGrade result = service.handle(new UpdateSchoolGradeCommand(id, "Primeiro ano", 5));

        assertEquals("Primeiro ano", result.getName());
        assertEquals(5, result.getGradeOrder());
    }

    @Test
    @DisplayName("Deve barrar nome em branco no domínio sem consultar a duplicidade")
    void handle_ShouldRejectBlankNameWithoutQueryingDuplicates() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);

        InternalException exception = assertThrows(InternalException.class,
                () -> service.handle(new UpdateSchoolGradeCommand(id, "   ", 2)));

        assertEquals("O nome da série escolar não pode ser vazio.", exception.getMessage());
        verify(repository, never()).existsByNameAndIdNot(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar pelo id da rota e não pelo id do corpo")
    void handle_ShouldUseIdFromCommand() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);
        when(repository.save(any(SchoolGrade.class))).thenAnswer(i -> i.getArgument(0));

        service.handle(new UpdateSchoolGradeCommand(null, "Segundo ano", 2).withId(id));

        verify(repository).findByIdOrThrow(id);
        ArgumentCaptor<SchoolGrade> captor = ArgumentCaptor.forClass(SchoolGrade.class);
        verify(repository).save(captor.capture());
        assertEquals("Segundo ano", captor.getValue().getName());
    }

    @Test
    @DisplayName("Não deve salvar quando a atualização deixa o estado inválido")
    void handle_ShouldNotSaveWhenUpdaterRejects() {
        SchoolGrade existing = SchoolGrade.create("Primeiro ano", 1);
        UUID id = existing.getId();
        when(repository.findByIdOrThrow(id)).thenReturn(existing);

        assertThrows(InternalException.class,
                () -> service.handle(new UpdateSchoolGradeCommand(id, "Segundo ano", 0)));

        verify(repository, never()).save(any());
    }
}
