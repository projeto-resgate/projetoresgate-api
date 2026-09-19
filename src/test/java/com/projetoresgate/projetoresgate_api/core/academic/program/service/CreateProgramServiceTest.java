package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.CreateProgramCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProgramService - Test")
class CreateProgramServiceTest {

    @Mock
    private ProgramRepository repository;

    @Mock
    private FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    @InjectMocks
    private CreateProgramService service;

    @Test
    @DisplayName("Deve criar programa sem categorias com sucesso")
    void handle_ShouldCreateSuccessfully() {
        CreateProgramCommand command = new CreateProgramCommand(
                "Programa A", "https://site.com", ProgramStatus.ACTIVE, null, null);

        when(repository.existsByName("Programa A")).thenReturn(false);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertNotNull(response);
        assertEquals("Programa A", response.name());
        assertEquals("https://site.com", response.webSiteUrl());
        assertEquals(ProgramStatus.ACTIVE, response.status());
        assertTrue(response.educatorCategoryItems().isEmpty());

        verify(repository).save(any(Program.class));
        verify(findLegalPersonSummariesUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("Deve criar programa com categorias de educador e instituição resolvida")
    void handle_ShouldCreateWithEducatorCategoryItems() {
        UUID institutionId = UUID.randomUUID();
        CreateProgramCommand.CreateEducatorCategoryItemCommand itemCommand =
                new CreateProgramCommand.CreateEducatorCategoryItemCommand("Fonoaudiólogo");
        CreateProgramCommand command = new CreateProgramCommand(
                "Programa A", null, ProgramStatus.ACTIVE, institutionId, List.of(itemCommand));

        LegalPersonSummaryResponse institution = new LegalPersonSummaryResponse(institutionId, "Escola X", "12345678000195");

        when(repository.existsByName("Programa A")).thenReturn(false);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));
        when(findLegalPersonSummariesUseCase.handle(Set.of(institutionId))).thenReturn(List.of(institution));

        ProgramResponse response = service.handle(command);

        assertEquals(1, response.educatorCategoryItems().size());
        assertEquals("Fonoaudiólogo", response.educatorCategoryItems().get(0).name());
        assertNotNull(response.institution());
        assertEquals("Escola X", response.institution().displayName());

        ArgumentCaptor<Program> captor = ArgumentCaptor.forClass(Program.class);
        verify(repository).save(captor.capture());
        Program saved = captor.getValue();
        assertEquals(1, saved.getEducatorCategoryItemList().size());
        EducatorCategoryItem item = saved.getEducatorCategoryItemList().get(0);
        assertEquals("Fonoaudiólogo", item.getName());
        assertEquals(saved, item.getProgram());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já está cadastrado")
    void handle_ShouldFailWhenNameAlreadyExists() {
        CreateProgramCommand command = new CreateProgramCommand(
                "Programa A", null, ProgramStatus.ACTIVE, null, null);

        when(repository.existsByName("Programa A")).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Já existe um programa cadastrado com este nome.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção de domínio quando o status é nulo")
    void handle_ShouldFailWhenStatusIsNull() {
        CreateProgramCommand command = new CreateProgramCommand(
                "Programa A", null, null, null, null);

        when(repository.existsByName("Programa A")).thenReturn(false);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("O status é obrigatório.", exception.getMessage());

        verify(repository, never()).save(any());
    }
}