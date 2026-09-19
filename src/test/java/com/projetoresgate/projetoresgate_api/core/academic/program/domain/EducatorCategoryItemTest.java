package com.projetoresgate.projetoresgate_api.core.academic.program.domain;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EducatorCategoryItem - Entity Test")
class EducatorCategoryItemTest {

    private Program buildProgram() {
        return Program.create("Programa A", null, ProgramStatus.ACTIVE, null);
    }

    @Test
    @DisplayName("Deve criar uma categoria de educador com sucesso")
    void create_ShouldSucceed() {
        Program program = buildProgram();

        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", program);

        assertNotNull(item);
        assertNotNull(item.getId());
        assertEquals("Fonoaudiólogo", item.getName());
        assertEquals(program, item.getProgram());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar categoria sem nome")
    void create_ShouldFailWithoutName() {
        InternalException exception = assertThrows(InternalException.class,
                () -> EducatorCategoryItem.create("", buildProgram()));
        assertEquals("O nome da categoria de educador não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar categoria sem programa")
    void create_ShouldFailWithoutProgram() {
        InternalException exception = assertThrows(InternalException.class,
                () -> EducatorCategoryItem.create("Fonoaudiólogo", null));
        assertEquals("O programa é obrigatório.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar o nome com sucesso")
    void updateName_ShouldSucceed() {
        Program program = buildProgram();
        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", program);

        item.updateName("Psicopedagogo");

        assertEquals("Psicopedagogo", item.getName());
        assertDoesNotThrow(item::validate);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar nome para vazio")
    void updateName_ShouldFailWhenBlank() {
        EducatorCategoryItem item = EducatorCategoryItem.create("Fonoaudiólogo", buildProgram());

        InternalException exception = assertThrows(InternalException.class, () -> item.updateName(""));
        assertEquals("O nome da categoria de educador não pode ser vazio.", exception.getMessage());
    }
}