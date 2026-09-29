package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.CreateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateSchoolGradeService - Test")
class CreateSchoolGradeServiceTest {

    @Mock
    private SchoolGradeRepository repository;

    @InjectMocks
    private CreateSchoolGradeService service;

    @Test
    @DisplayName("Deve criar a série com o id gerado e devolver a entidade salva")
    void handle_ShouldCreateSchoolGrade() {
        when(repository.existsByName("Primeiro ano")).thenReturn(false);
        when(repository.save(any(SchoolGrade.class))).thenAnswer(i -> i.getArgument(0));

        SchoolGrade result = service.handle(new CreateSchoolGradeCommand("Primeiro ano", 1));

        assertNotNull(result.getId());
        assertEquals("Primeiro ano", result.getName());
        assertEquals(1, result.getGradeOrder());

        verify(repository).save(any(SchoolGrade.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já está cadastrado e não salvar nada")
    void handle_ShouldFailWhenNameAlreadyExists() {
        when(repository.existsByName("Primeiro ano")).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class,
                () -> service.handle(new CreateSchoolGradeCommand("Primeiro ano", 1)));

        assertEquals("Já existe uma série escolar cadastrada com este nome.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve delegar a validação de domínio e não salvar quando a ordem é inválida")
    void handle_ShouldNotSaveWhenDomainValidationFails() {
        when(repository.existsByName("Primeiro ano")).thenReturn(false);

        assertThrows(InternalException.class,
                () -> service.handle(new CreateSchoolGradeCommand("Primeiro ano", 0)));

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve persistir o nome exatamente como recebido, sem normalizar")
    void handle_ShouldPersistNameAsReceived() {
        when(repository.existsByName("Primeiro ano do Ensino Médio")).thenReturn(false);
        when(repository.save(any(SchoolGrade.class))).thenAnswer(i -> i.getArgument(0));

        service.handle(new CreateSchoolGradeCommand("Primeiro ano do Ensino Médio", 1));

        ArgumentCaptor<SchoolGrade> captor = ArgumentCaptor.forClass(SchoolGrade.class);
        verify(repository).save(captor.capture());
        assertEquals("Primeiro ano do Ensino Médio", captor.getValue().getName());
    }
}
