package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Comandos de pessoa física - Test")
class NaturalPersonCommandTest {

    @Test
    @DisplayName("Criar deve tratar campos opcionais vazios como não informados")
    void create_ShouldTurnBlankOptionalFieldsIntoNull() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Ana", "ana@test.com", "", " ", "", null, "", "  ", null, null);

        assertNull(command.nickname());
        assertNull(command.rg());
        assertNull(command.cpf());
        assertNull(command.phone());
        assertNull(command.cellphone());
    }

    @Test
    @DisplayName("Editar deve tratar campos opcionais vazios como não informados")
    void update_ShouldTurnBlankOptionalFieldsIntoNull() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                UUID.randomUUID(), "Ana", "ana@test.com", "", "", " ", null, "", "", null, null);

        assertNull(command.nickname());
        assertNull(command.rg());
        assertNull(command.cpf());
        assertNull(command.phone());
        assertNull(command.cellphone());
    }

    @Test
    @DisplayName("Criar deve guardar só o valor, sem máscara")
    void create_ShouldRemoveMasks() {
        CreateNaturalPersonCommand command = new CreateNaturalPersonCommand(
                "Ana", "ana@test.com", null, "mg-12.345.678", "123.456.789-09", null,
                "(47) 3333-4444", "(47) 99999-0000", null, null);

        assertEquals("MG12345678", command.rg());
        assertEquals("12345678909", command.cpf());
        assertEquals("4733334444", command.phone());
        assertEquals("47999990000", command.cellphone());
    }

    @Test
    @DisplayName("Editar deve guardar só o valor, sem máscara")
    void update_ShouldRemoveMasks() {
        UpdateNaturalPersonCommand command = new UpdateNaturalPersonCommand(
                UUID.randomUUID(), "Ana", "ana@test.com", null, "123.456.789-09", "123.456.789-09", null,
                "(47) 3333-4444", "(47) 99999-0000", null, null);

        assertEquals("12345678909", command.rg());
        assertEquals("12345678909", command.cpf());
        assertEquals("4733334444", command.phone());
        assertEquals("47999990000", command.cellphone());
    }
}
