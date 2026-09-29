package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SchoolGrade - Entity Test")
class SchoolGradeTest {

    @Test
    @DisplayName("Deve criar uma série escolar com sucesso")
    void create_ShouldSucceed() {
        SchoolGrade schoolGrade = SchoolGrade.create("Primeiro ano do ensino médio", 1);

        assertNotNull(schoolGrade);
        assertNotNull(schoolGrade.getId());
        assertEquals("Primeiro ano do ensino médio", schoolGrade.getName());
        assertEquals(1, schoolGrade.getGradeOrder());
    }

    @Test
    @DisplayName("Deve gerar ids diferentes para séries criadas em sequência")
    void create_ShouldGenerateDistinctIds() {
        SchoolGrade first = SchoolGrade.create("Primeiro ano", 1);
        SchoolGrade second = SchoolGrade.create("Segundo ano", 2);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar série sem nome")
    void create_ShouldFailWithoutName() {
        InternalException exception = assertThrows(InternalException.class,
                () -> SchoolGrade.create("  ", 1));
        assertEquals("O nome da série escolar não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar série com nome acima de 255 caracteres")
    void create_ShouldFailWhenNameTooLong() {
        InternalException exception = assertThrows(InternalException.class,
                () -> SchoolGrade.create("a".repeat(256), 1));
        assertEquals("O nome da série escolar não pode exceder 255 caracteres.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar nome com exatamente 255 caracteres")
    void create_ShouldAcceptNameAtMaxLength() {
        SchoolGrade schoolGrade = SchoolGrade.create("a".repeat(255), 1);

        assertEquals(255, schoolGrade.getName().length());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar série sem ordem")
    void create_ShouldFailWithoutGradeOrder() {
        InternalException exception = assertThrows(InternalException.class,
                () -> SchoolGrade.create("Primeiro ano", null));
        assertEquals("A ordem da série escolar é obrigatória.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar série com ordem zero ou negativa")
    void create_ShouldFailWhenGradeOrderNotPositive() {
        InternalException zeroException = assertThrows(InternalException.class,
                () -> SchoolGrade.create("Primeiro ano", 0));
        assertEquals("A ordem da série escolar deve ser maior que zero.", zeroException.getMessage());

        InternalException negativeException = assertThrows(InternalException.class,
                () -> SchoolGrade.create("Primeiro ano", -3));
        assertEquals("A ordem da série escolar deve ser maior que zero.", negativeException.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar nome e ordem via Updater com sucesso")
    void updater_ShouldUpdateFields() {
        SchoolGrade schoolGrade = SchoolGrade.create("Primeiro ano", 1);

        schoolGrade.update()
                .name("Segundo ano")
                .gradeOrder(2)
                .apply();

        assertEquals("Segundo ano", schoolGrade.getName());
        assertEquals(2, schoolGrade.getGradeOrder());
        assertDoesNotThrow(schoolGrade::validate);
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar série com nome apagado na atualização")
    void updater_ShouldFailWhenNameBlanked() {
        SchoolGrade schoolGrade = SchoolGrade.create("Primeiro ano", 1);

        InternalException exception = assertThrows(InternalException.class,
                () -> schoolGrade.update().name("").apply());
        assertEquals("O nome da série escolar não pode ser vazio.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar série sem ordem na atualização")
    void updater_ShouldFailWhenGradeOrderNull() {
        SchoolGrade schoolGrade = SchoolGrade.create("Primeiro ano", 1);

        InternalException exception = assertThrows(InternalException.class,
                () -> schoolGrade.update().gradeOrder(null).apply());
        assertEquals("A ordem da série escolar é obrigatória.", exception.getMessage());
    }

    @Test
    @DisplayName("O apply deve devolver a própria entidade já com os campos alterados")
    void updater_ShouldReturnSameEntity() {
        SchoolGrade schoolGrade = SchoolGrade.create("Primeiro ano", 1);

        SchoolGrade result = schoolGrade.update().name("Segundo ano").gradeOrder(2).apply();

        assertSame(schoolGrade, result);
        assertEquals("Segundo ano", result.getName());
        assertEquals(2, result.getGradeOrder());
    }
}
