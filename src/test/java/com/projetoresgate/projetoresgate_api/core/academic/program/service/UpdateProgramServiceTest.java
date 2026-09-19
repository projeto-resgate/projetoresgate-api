package com.projetoresgate.projetoresgate_api.core.academic.program.service;

import com.projetoresgate.projetoresgate_api.core.academic.program.api.dto.ProgramResponse;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.UpdateProgramCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProgramService - Test")
class UpdateProgramServiceTest {

    @Mock
    private ProgramRepository repository;

    @Mock
    private EducatorCategoryItemRepository itemRepository;

    @Mock
    private FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    @InjectMocks
    private UpdateProgramService service;

    private Program program;
    private UUID programId;
    private EducatorCategoryItem existingItem;

    @BeforeEach
    void setUp() {
        program = Program.create("Programa A", "https://site.com", ProgramStatus.ACTIVE, null);
        programId = program.getId();
        existingItem = EducatorCategoryItem.create("Fonoaudiólogo", program);
        program.getEducatorCategoryItemList().add(existingItem);
    }

    @Test
    @DisplayName("Deve atualizar nome, site e status do programa com sucesso")
    void handle_ShouldUpdateBasicFields() {
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa B", "https://novo-site.com", ProgramStatus.INACTIVE, null);

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.existsByNameAndIdNot("Programa B", programId)).thenReturn(false);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertEquals("Programa B", response.name());
        assertEquals("https://novo-site.com", response.webSiteUrl());
        assertEquals(ProgramStatus.INACTIVE, response.status());
        assertEquals("Programa B", program.getName());
        verify(repository).save(any(Program.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o nome já pertence a outro programa")
    void handle_ShouldFailWhenNameAlreadyExistsInAnotherProgram() {
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa B", null, ProgramStatus.ACTIVE, null);

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.existsByNameAndIdNot("Programa B", programId)).thenReturn(true);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertEquals("Já existe um programa cadastrado com este nome.", exception.getMessage());

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar nova categoria de educador quando o item não possui id")
    void reconcile_ShouldCreateNewItem() {
        UpdateProgramCommand.UpdateEducatorCategoryItemCommand newItem =
                new UpdateProgramCommand.UpdateEducatorCategoryItemCommand(null, "Psicopedagogo");
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa A", null, ProgramStatus.ACTIVE, List.of(newItem));

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertEquals(1, response.educatorCategoryItems().size());
        assertNotEquals(existingItem.getId(), response.educatorCategoryItems().get(0).id());
        assertEquals("Psicopedagogo", response.educatorCategoryItems().get(0).name());
        verify(itemRepository).delete(existingItem);
    }

    @Test
    @DisplayName("Deve manter categoria existente quando o id for informado")
    void reconcile_ShouldKeepExistingItemWhenIdInformed() {
        UpdateProgramCommand.UpdateEducatorCategoryItemCommand keptItem =
                new UpdateProgramCommand.UpdateEducatorCategoryItemCommand(existingItem.getId(), "Fonoaudiólogo");
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa A", null, ProgramStatus.ACTIVE, List.of(keptItem));

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertEquals(1, response.educatorCategoryItems().size());
        assertEquals(existingItem.getId(), response.educatorCategoryItems().get(0).id());
        assertEquals("Fonoaudiólogo", response.educatorCategoryItems().get(0).name());
        verify(itemRepository, never()).delete(any(EducatorCategoryItem.class));
    }

    @Test
    @DisplayName("Deve atualizar o nome da categoria existente quando for diferente")
    void reconcile_ShouldUpdateItemName() {
        UpdateProgramCommand.UpdateEducatorCategoryItemCommand renamedItem =
                new UpdateProgramCommand.UpdateEducatorCategoryItemCommand(existingItem.getId(), "Novo Nome");
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa A", null, ProgramStatus.ACTIVE, List.of(renamedItem));

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertEquals("Novo Nome", response.educatorCategoryItems().get(0).name());
        assertEquals("Novo Nome", existingItem.getName());
        verify(itemRepository, never()).delete(any(EducatorCategoryItem.class));
    }

    @Test
    @DisplayName("Deve remover categoria não informada na requisição")
    void reconcile_ShouldRemoveItemNotInRequest() {
        EducatorCategoryItem removedItem = EducatorCategoryItem.create("Removida", program);
        program.getEducatorCategoryItemList().add(removedItem);

        UpdateProgramCommand.UpdateEducatorCategoryItemCommand keptItem =
                new UpdateProgramCommand.UpdateEducatorCategoryItemCommand(existingItem.getId(), "Fonoaudiólogo");
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa A", null, ProgramStatus.ACTIVE, List.of(keptItem));

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertEquals(1, response.educatorCategoryItems().size());
        assertEquals(existingItem.getId(), response.educatorCategoryItems().get(0).id());
        assertEquals("Fonoaudiólogo", response.educatorCategoryItems().get(0).name());
        assertEquals(1, program.getEducatorCategoryItemList().size());
        verify(itemRepository).delete(removedItem);
    }

    @Test
    @DisplayName("Deve remover todas as categorias quando a lista da requisição for nula ou vazia")
    void reconcile_ShouldRemoveAllWhenRequestEmpty() {
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa A", null, ProgramStatus.ACTIVE, null);

        when(repository.findByIdOrThrow(programId)).thenReturn(program);
        when(repository.save(any(Program.class))).thenAnswer(i -> i.getArgument(0));

        ProgramResponse response = service.handle(command);

        assertTrue(response.educatorCategoryItems().isEmpty());
        assertTrue(program.getEducatorCategoryItemList().isEmpty());
        verify(itemRepository).delete(existingItem);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a categoria informada não pertence ao programa")
    void reconcile_ShouldFailWhenItemBelongsToAnotherProgram() {
        UUID foreignId = UUID.randomUUID();
        UpdateProgramCommand.UpdateEducatorCategoryItemCommand foreignItem =
                new UpdateProgramCommand.UpdateEducatorCategoryItemCommand(foreignId, "Estrangeira");
        UpdateProgramCommand command = new UpdateProgramCommand(
                programId, "Programa A", null, ProgramStatus.ACTIVE, List.of(foreignItem));

        when(repository.findByIdOrThrow(programId)).thenReturn(program);

        InternalException exception = assertThrows(InternalException.class, () -> service.handle(command));
        assertTrue(exception.getMessage().contains("não pertence ao programa"));
        verify(itemRepository, never()).delete(any(EducatorCategoryItem.class));
        verify(repository, never()).save(any());
    }
}