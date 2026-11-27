package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ClassGroup - Test")
class ClassGroupTest {

    private ClassGroup givenClassGroup(String programName, List<SchoolGradeRef> schoolGrades) {
        return ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, programName, schoolGrades);
    }

    @Test
    @DisplayName("Deve compor o título com programa e séries ordenadas e sem duplicatas")
    void create_ShouldComposeTitleWithProgramAndGrades() {
        ClassGroup classGroup = givenClassGroup("Programa A", List.of(
                SchoolGradeRef.of(UUID.randomUUID(), "2ª Série", "2ª", 2),
                SchoolGradeRef.of(UUID.randomUUID(), "1ª Série", "1ª", 1),
                SchoolGradeRef.of(UUID.randomUUID(), "1ª Série", "1ª", 1)));

        assertEquals("Programa A - 1ª, 2ª", classGroup.getTitle());
    }

    @Test
    @DisplayName("Deve usar o nome da série quando ela não tiver abreviação")
    void create_ShouldFallBackToGradeNameWithoutAbbreviation() {
        ClassGroup classGroup = givenClassGroup("Programa A", List.of(
                SchoolGradeRef.of(UUID.randomUUID(), "1ª Série", "1ª", 1),
                SchoolGradeRef.of(UUID.randomUUID(), "Educação Infantil", null, 3)));

        assertEquals("Programa A - 1ª, Educação Infantil", classGroup.getTitle());
    }

    @Test
    @DisplayName("Deve atualizar o título concatenando programa, séries e dias da semana abreviados")
    void refreshTitle_ShouldJoinAllParts() {
        ClassGroup classGroup = givenClassGroup("Programa A", List.of(
                SchoolGradeRef.of(UUID.randomUUID(), "1ª Série", "1ª", 1), SchoolGradeRef.of(UUID.randomUUID(), "2ª Série", "2ª", 2)));

        classGroup.refreshTitle("Programa A", List.of(WeekDay.WEDNESDAY, WeekDay.MONDAY));

        assertEquals("Programa A - 1ª, 2ª - Seg, Qua", classGroup.getTitle());
    }

    @Test
    @DisplayName("Deve omitir as partes vazias")
    void refreshTitle_ShouldOmitEmptyParts() {
        ClassGroup classGroup = givenClassGroup("Programa A", List.of());

        classGroup.refreshTitle("Programa A", List.of());
        assertEquals("Programa A", classGroup.getTitle());

        classGroup.refreshTitle(null, List.of(WeekDay.FRIDAY));
        assertEquals("Sex", classGroup.getTitle());
    }

    @Test
    @DisplayName("Deve lançar erro quando não for possível gerar o título")
    void refreshTitle_ShouldFailWhenNoPart() {
        ClassGroup classGroup = givenClassGroup("Programa A", List.of());

        assertThrows(InternalException.class, () -> classGroup.refreshTitle(null, null));
    }


    @Test
    @DisplayName("É ativa só com a data de hoje entre o início e o término, inclusive nos dois extremos")
    void isActiveOn_ShouldDependOnPeriod() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2025, 2, 2, 0, 0), 1, null, "Programa A", List.of());

        assertTrue(classGroup.isActiveOn(LocalDateTime.of(2025, 2, 2, 0, 0)));
        assertTrue(classGroup.isActiveOn(LocalDateTime.of(2025, 2, 20, 10, 0)));
        assertTrue(classGroup.isActiveOn(LocalDateTime.of(2025, 3, 2, 0, 0)));
        assertFalse(classGroup.isActiveOn(LocalDateTime.of(2025, 2, 1, 23, 59)));
        assertFalse(classGroup.isActiveOn(LocalDateTime.of(2025, 3, 2, 0, 1)));
    }

    @Test
    @DisplayName("Turma desativada não é ativa, mesmo dentro do período")
    void isActiveOn_ShouldBeFalseWhenSuspended() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2025, 2, 2, 0, 0), 1, null, "Programa A", List.of());
        classGroup.suspend();

        assertFalse(classGroup.isActiveOn(LocalDateTime.of(2025, 2, 20, 10, 0)));
    }
}
