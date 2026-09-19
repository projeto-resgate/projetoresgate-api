package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.SoftDeleteProgramCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoftDeleteProgramService - Test")
class SoftDeleteProgramServiceTest {

    @Mock
    private ProgramRepository repository;

    @InjectMocks
    private SoftDeleteProgramService service;

    @Test
    @DisplayName("Deve deletar programa com sucesso")
    void handle_ShouldDeleteSuccessfully() {
        UUID id = UUID.randomUUID();
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, null);
        SoftDeleteProgramCommand command = new SoftDeleteProgramCommand(id);

        when(repository.findByIdOrThrow(id)).thenReturn(program);

        service.handle(command);

        verify(repository).delete(program);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o programa não for encontrado")
    void handle_ShouldFailWhenProgramNotFound() {
        UUID id = UUID.randomUUID();
        SoftDeleteProgramCommand command = new SoftDeleteProgramCommand(id);

        when(repository.findByIdOrThrow(id)).thenThrow(new ResourceNotFoundException("Programa não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(command));

        verify(repository, never()).delete(any(Program.class));
    }
}