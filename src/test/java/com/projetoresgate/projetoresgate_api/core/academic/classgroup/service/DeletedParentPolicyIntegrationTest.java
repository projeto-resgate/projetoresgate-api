package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.SchoolGradeRefRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassScheduleCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassGroupCommand;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindClassGroupByIdQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.service.SoftDeleteProgramService;
import com.projetoresgate.projetoresgate_api.core.academic.program.usecase.command.SoftDeleteProgramCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service.SoftDeleteSchoolGradeService;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service.UpdateSchoolGradeService;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.service.FindLegalPersonSummariesService;
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
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Política de exclusão de pais da turma - Integração")
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
    private FindAllClassGroupsService findAll;
    private FindClassGroupByIdService findById;
    private CreateClassScheduleService createSchedule;
    private SoftDeleteProgramService deleteProgram;
    private SoftDeleteLegalPersonService deleteInstitution;
    private SoftDeleteSchoolGradeService deleteSchoolGrade;
    private UpdateSchoolGradeService updateSchoolGrade;

    @BeforeEach
    void setUp() {
        createClassGroup = new CreateClassGroupService(classGroupRepository, programRepository,
                legalPersonRepository, schoolGradeRepository, schoolGradeRefRepository);
        updateClassGroup = new UpdateClassGroupService(classGroupRepository, programRepository,
                legalPersonRepository, schoolGradeRepository, schoolGradeRefRepository, scheduleRepository);
        findAll = new FindAllClassGroupsService(classGroupRepository, programRepository,
                new FindLegalPersonSummariesService(legalPersonRepository));
        findById = new FindClassGroupByIdService(classGroupRepository, programRepository, legalPersonRepository);
        createSchedule = new CreateClassScheduleService(classGroupRepository, scheduleRepository, programRepository);
        deleteProgram = new SoftDeleteProgramService(programRepository);
        deleteInstitution = new SoftDeleteLegalPersonService(legalPersonRepository);
        deleteSchoolGrade = new SoftDeleteSchoolGradeService(schoolGradeRepository);
        updateSchoolGrade = new UpdateSchoolGradeService(schoolGradeRepository,
                new RefreshClassGroupTitlesService(classGroupRepository, scheduleRepository, programRepository));
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

    private List<ClassGroupSummaryResponse> listAll() {
        return findAll.handle(new FindAllClassGroupsQuery(null, null, null, null, PageRequest.of(0, 50))).getContent();
    }

    @Test
    @DisplayName("Programa excluído: a turma continua na listagem e no detalhe, com o nome do programa")
    void deletedProgram_ClassGroupStaysVisibleWithProgramName() {
        Program program = givenProgram("Reforço Escolar");
        ClassGroupResponse created = givenClassGroup(program, givenInstitution("Escola A"));

        deleteProgram.handle(new SoftDeleteProgramCommand(program.getId()));
        flushAndClear();

        List<ClassGroupSummaryResponse> list = listAll().stream()
                .filter(c -> c.id().equals(created.id())).toList();
        assertEquals(1, list.size(), "a turma não pode sumir da listagem");
        assertEquals("Reforço Escolar", list.get(0).programName(), "o nome do programa excluído continua aparecendo");
        assertEquals("Escola A", list.get(0).institutionName());

        ClassGroupResponse detail = findById.handle(new FindClassGroupByIdQuery(created.id()));
        assertEquals("Reforço Escolar", detail.programName());
    }

    @Test
    @DisplayName("Programa excluído: a turma existente continua editável e aceita novos horários (o título é recalculado)")
    void deletedProgram_ClassGroupStaysEditable() {
        Program program = givenProgram("Arte e Cultura");
        ClassGroupResponse created = givenClassGroup(program, givenInstitution("Escola B"));
        deleteProgram.handle(new SoftDeleteProgramCommand(program.getId()));
        flushAndClear();

        ClassGroupResponse updated = assertDoesNotThrow(() -> updateClassGroup.handle(new UpdateClassGroupCommand(
                created.id(), program.getId(), created.institutionId(), null, 45, null, null, "editada", null)));
        assertEquals(45, updated.studentProjections());
        assertEquals("Arte e Cultura", updated.programName());

        assertDoesNotThrow(() -> createSchedule.handle(new CreateClassScheduleCommand(
                created.id(), WeekDay.MONDAY, LocalTime.of(8, 0), LocalTime.of(9, 0))));
        flushAndClear();

        assertEquals("Arte e Cultura - Seg", findById.handle(new FindClassGroupByIdQuery(created.id())).title());
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
    @DisplayName("Instituição excluída (com o endereço): a turma continua visível com o nome da instituição")
    void deletedInstitution_ClassGroupStaysVisibleWithInstitutionName() {
        LegalPerson institution = givenInstitution("Colégio Esperança");
        ClassGroupResponse created = givenClassGroup(givenProgram("Matemática"), institution);

        deleteInstitution.handle(new SoftDeleteLegalPersonCommand(institution.getId()));
        flushAndClear();

        ClassGroupSummaryResponse summary = listAll().stream()
                .filter(c -> c.id().equals(created.id())).findFirst().orElseThrow();
        assertEquals("Colégio Esperança", summary.institutionName());
        assertEquals("Colégio Esperança", findById.handle(new FindClassGroupByIdQuery(created.id())).institutionName());
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
    @DisplayName("Série excluída: a turma continua mostrando a série e o título não muda")
    void deletedSchoolGrade_ClassGroupKeepsGradeAndTitle() {
        SchoolGrade grade = givenGrade("7º ano", "7º", 7);
        ClassGroupResponse created = givenClassGroup(givenProgram("Ciências"), givenInstitution("Escola F"), grade);
        assertEquals("Ciências - 7º", created.title());

        deleteSchoolGrade.handle(new SoftDeleteSchoolGradeCommand(grade.getId()));
        flushAndClear();

        ClassGroupResponse detail = findById.handle(new FindClassGroupByIdQuery(created.id()));
        assertEquals(1, detail.schoolGrades().size(), "a série excluída continua na turma");
        assertEquals(grade.getId(), detail.schoolGrades().get(0).id());
        assertEquals("Ciências - 7º", detail.title(), "o título não perde a série excluída");
    }

    @Test
    @DisplayName("Série excluída: a turma continua editável, mantendo a série; renomear a série ainda atualiza o título")
    void deletedSchoolGrade_ClassGroupStaysEditable() {
        SchoolGrade kept = givenGrade("8º ano", "8º", 8);
        SchoolGrade other = givenGrade("9º ano", "9º", 9);
        ClassGroupResponse created = givenClassGroup(givenProgram("História"), givenInstitution("Escola G"), kept, other);
        deleteSchoolGrade.handle(new SoftDeleteSchoolGradeCommand(kept.getId()));
        flushAndClear();

        ClassGroupResponse updated = assertDoesNotThrow(() -> updateClassGroup.handle(new UpdateClassGroupCommand(
                created.id(), null, null, List.of(kept.getId(), other.getId()), 40, null, null, null, null)));
        assertEquals(2, updated.schoolGrades().size());
        assertEquals("História - 8º, 9º", updated.title());
        flushAndClear();

        updateSchoolGrade.handle(new UpdateSchoolGradeCommand(other.getId(), other.getName(), "9ºA", 9));
        flushAndClear();
        assertEquals("História - 8º, 9ºA", findById.handle(new FindClassGroupByIdQuery(created.id())).title());
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
}
