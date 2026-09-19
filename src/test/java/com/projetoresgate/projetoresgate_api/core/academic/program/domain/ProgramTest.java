package com.projetoresgate.projetoresgate_api.core.academic.program.domain;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Program - Entity Test")
class ProgramTest {

    @Test
    @DisplayName("Deve criar um programa com sucesso e iniciar lista de categorias vazia")
    void create_ShouldSucceed() {
        UUID institutionId = UUID.randomUUID();

        Program program = Program.create("Programa A", "https://site.com", ProgramStatus.ACTIVE, institutionId);

        assertNotNull(program);
        assertNotNull(program.getId());
        assertEquals("Programa A", program.getName());
        assertEquals("https://site.com", program.getWebSiteUrl());
        assertEquals(ProgramStatus.ACTIVE, program.getStatus());
        assertEquals(institutionId, program.getInstitutionId());
        assertNotNull(program.getEducatorCategoryItemList());
        assertTrue(program.getEducatorCategoryItemList().isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar programa sem nome")
    void create_ShouldFailWithoutName() {
        InternalException exception = assertThrows(InternalException.class,
                () -> Program.create("", "https://site.com", ProgramStatus.ACTIVE, null));
        assertEquals("O nome do programa não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar programa sem status")
    void create_ShouldFailWithoutStatus() {
        InternalException exception = assertThrows(InternalException.class,
                () -> Program.create("Programa A", "https://site.com", null, null));
        assertEquals("O status é obrigatório.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar nome, site e status via Updater com sucesso")
    void updater_ShouldUpdateFields() {
        Program program = Program.create("Programa A", "https://site.com", ProgramStatus.ACTIVE, null);

        program.update()
                .name("Programa B")
                .webSiteUrl("https://novo-site.com")
                .status(ProgramStatus.INACTIVE)
                .apply();

        assertEquals("Programa B", program.getName());
        assertEquals("https://novo-site.com", program.getWebSiteUrl());
        assertEquals(ProgramStatus.INACTIVE, program.getStatus());
        assertDoesNotThrow(program::validate);
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar programa com nome apagado na atualização")
    void updater_ShouldFailWhenNameBlanked() {
        Program program = Program.create("Programa A", "https://site.com", ProgramStatus.ACTIVE, null);

        InternalException exception = assertThrows(InternalException.class,
                () -> program.update().name("").apply());
        assertEquals("O nome do programa não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve permitir adicionar categoria de educador à lista")
    void educatorCategoryItemList_ShouldAcceptItems() {
        Program program = Program.create("Programa A", null, ProgramStatus.ACTIVE, null);

        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", program);
        program.getEducatorCategoryItemList().add(item);

        assertEquals(1, program.getEducatorCategoryItemList().size());
        assertEquals(item, program.getEducatorCategoryItemList().get(0));
    }
}