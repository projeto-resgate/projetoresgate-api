package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.SchoolGradeRefRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.service.SoftDeleteProgramService;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.SoftDeleteProgramCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service.SoftDeleteSchoolGradeService;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.service.SoftDeleteLegalPersonService;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.SoftDeleteLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Exclusão de programa, instituição e série da turma - Integração")
class DeletedParentPolicyIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static final LocalDateTime START = LocalDateTime.of(2026, 2, 1, 8, 0);

    @Autowired private ClassGroupRepository classGroupRepository;
    @Autowired private ClassScheduleRepository scheduleRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private LegalPersonRepository legalPersonRepository;
    @Autowired private SchoolGradeRepository schoolGradeRepository;
    @Autowired private SchoolGradeRefRepository schoolGradeRefRepository;
    @Autowired private EntityManager entityManager;

    private CreateClassGroupService createClassGroup;
    private UpdateClassGroupService updateClassGroup;
    private SoftDeleteProgramService deleteProgram;
    private SoftDeleteLegalPersonService deleteInstitution;
    private SoftDeleteSchoolGradeService deleteSchoolGrade;

    @BeforeEach
    void setUp() {
        createClassGroup = new CreateClassGroupService(classGroupRepository, programRepository,
                legalPersonRepository, schoolGradeRepository, schoolGradeRefRepository);
        updateClassGroup = new UpdateClassGroupService(classGroupRepository, programRepository,
                legalPersonRepository, schoolGradeRefRepository, scheduleRepository);
        deleteProgram = new SoftDeleteProgramService(programRepository);
        deleteInstitution = new SoftDeleteLegalPersonService(legalPersonRepository);
        deleteSchoolGrade = new SoftDeleteSchoolGradeService(schoolGradeRepository);
    }

    private Program givenProgram(String name) {
        return programRepository.save(Program.create(name, null, ProgramStatus.ACTIVE, null));
    }

    private LegalPerson givenInstitution(String name) {
        int n = SEQUENCE.incrementAndGet();
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null, "Centro", "Fortaleza", "CE");
        return legalPersonRepository.save(LegalPerson.create(String.format("%014d", 50000000000000L + n),
                name, name, name, null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
    }

    private SchoolGrade givenGrade(String name, String abbreviation, int order) {
        return schoolGradeRepository.save(SchoolGrade.create(name + SEQUENCE.incrementAndGet(), abbreviation, order));
    }

    private ClassGroupResponse givenClassGroup(Program program, LegalPerson institution, SchoolGrade... grades) {
        ClassGroupResponse created = createClassGroup.handle(new CreateClassGroupCommand(
                program.getId(), institution.getId(), List.of(grades).stream().map(SchoolGrade::getId).toList(),
                30, START, 6, null));
        entityManager.flush();
        entityManager.clear();
        return created;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Programa excluído: não é possível criar turma nova para ele")
    void deletedProgram_CannotCreateNewClassGroup() {
        Program program = givenProgram("Esporte");
        LegalPerson institution = givenInstitution("Escola C");
        deleteProgram.handle(new SoftDeleteProgramCommand(program.getId()));
        flushAndClear();

        assertThrows(ResourceNotFoundException.class, () -> createClassGroup.handle(new CreateClassGroupCommand(
                program.getId(), institution.getId(), null, 30, START, 6, null)));
    }

    @Test
    @DisplayName("Programa excluído: não é possível mover uma turma existente para ele")
    void deletedProgram_CannotMoveExistingClassGroupToIt() {
        Program deleted = givenProgram("Programa Excluído");
        Program active = givenProgram("Programa Ativo");
        LegalPerson institution = givenInstitution("Escola D");
        ClassGroupResponse created = givenClassGroup(active, institution);
        deleteProgram.handle(new SoftDeleteProgramCommand(deleted.getId()));
        flushAndClear();

        assertThrows(ResourceNotFoundException.class, () -> updateClassGroup.handle(new UpdateClassGroupCommand(
                created.id(), deleted.getId(), null, null, null, null, null, null, null)));
    }

    @Test
    @DisplayName("Instituição excluída: não é possível criar turma nova para ela")
    void deletedInstitution_CannotCreateNewClassGroup() {
        Program program = givenProgram("Leitura");
        LegalPerson institution = givenInstitution("Escola E");
        deleteInstitution.handle(new SoftDeleteLegalPersonCommand(institution.getId()));
        flushAndClear();

        assertThrows(ResourceNotFoundException.class, () -> createClassGroup.handle(new CreateClassGroupCommand(
                program.getId(), institution.getId(), null, 30, START, 6, null)));
    }

    @Test
    @DisplayName("Série excluída: não é possível vinculá-la a turma nova nem adicioná-la a uma turma existente")
    void deletedSchoolGrade_CannotBeLinkedAnew() {
        SchoolGrade grade = givenGrade("6º ano", "6º", 6);
        Program program = givenProgram("Geografia");
        LegalPerson institution = givenInstitution("Escola H");
        ClassGroupResponse existing = givenClassGroup(program, institution);
        deleteSchoolGrade.handle(new SoftDeleteSchoolGradeCommand(grade.getId()));
        flushAndClear();

        assertThrows(RuntimeException.class, () -> createClassGroup.handle(new CreateClassGroupCommand(
                program.getId(), institution.getId(), List.of(grade.getId()), 30, START, 6, null)));
        assertThrows(RuntimeException.class, () -> updateClassGroup.handle(new UpdateClassGroupCommand(
                existing.id(), null, null, List.of(grade.getId()), null, null, null, null, null)));
    }

    @Test
    @DisplayName("Não é possível excluir programa, instituição ou série que ainda tenham turma")
    void parentsWithClassGroup_CannotBeDeleted() {
        Program program = givenProgram("Robótica");
        LegalPerson institution = givenInstitution("Escola I");
        SchoolGrade grade = givenGrade("5º ano", "5º", 5);
        givenClassGroup(program, institution, grade);

        IllegalStateException programError = assertThrows(IllegalStateException.class,
                () -> deleteProgram.handle(new SoftDeleteProgramCommand(program.getId())));
        IllegalStateException institutionError = assertThrows(IllegalStateException.class,
                () -> deleteInstitution.handle(new SoftDeleteLegalPersonCommand(institution.getId())));
        IllegalStateException gradeError = assertThrows(IllegalStateException.class,
                () -> deleteSchoolGrade.handle(new SoftDeleteSchoolGradeCommand(grade.getId())));
        flushAndClear();

        assertEquals("Não é possível excluir o programa porque existem vínculos com: 1 turma. "
                + "Remova os vínculos antes de excluir.", programError.getMessage());
        assertTrue(institutionError.getMessage().contains("1 turma"));
        assertFalse(institutionError.getMessage().contains("programa"));
        assertTrue(gradeError.getMessage().contains("1 turma"));
        assertTrue(programRepository.findById(program.getId()).isPresent());
        assertTrue(legalPersonRepository.findById(institution.getId()).isPresent());
        assertTrue(schoolGradeRepository.findById(grade.getId()).isPresent());
    }
}
