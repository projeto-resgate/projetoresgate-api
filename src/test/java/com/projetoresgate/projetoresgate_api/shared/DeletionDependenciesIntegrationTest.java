package com.projetoresgate.projetoresgate_api.shared;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DisplayName("Contagem de dependências para bloquear exclusão - Integração")
class DeletionDependenciesIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Autowired private ClassGroupRepository classGroupRepository;
    @Autowired private ClassLearnerRepository classLearnerRepository;
    @Autowired private ClassCollaboratorRepository classCollaboratorRepository;
    @Autowired private CollaboratorRepository collaboratorRepository;
    @Autowired private LearnerRepository learnerRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private SchoolGradeRepository schoolGradeRepository;
    @Autowired private LegalPersonRepository legalPersonRepository;
    @Autowired private NaturalPersonRepository naturalPersonRepository;
    @Autowired private FamilyGroupRepository familyGroupRepository;
    @Autowired private EntityManager entityManager;

    private Program program;
    private LegalPerson institution;
    private ClassGroup classGroup;

    @BeforeEach
    void setUp() {
        program = Program.create("Programa Dependências " + SEQUENCE.incrementAndGet(), null, ProgramStatus.ACTIVE, null);
        program.getEducatorCategoryItemList().add(EducatorCategoryItem.create("Professor", program));
        program = programRepository.save(program);
        institution = givenLegalPerson("Escola");
        classGroup = givenClassGroup(institution);
    }

    private LegalPerson givenLegalPerson(String name) {
        int sequence = SEQUENCE.incrementAndGet();
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null, "Centro", "Fortaleza", "CE");
        return legalPersonRepository.save(LegalPerson.create(String.format("%014d", 90000000000000L + sequence),
                name + " " + sequence, null, null, null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
    }

    private ClassGroup givenClassGroup(LegalPerson institution) {
        return classGroupRepository.save(ClassGroup.create(program.getId(), institution.getId(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Turma " + SEQUENCE.incrementAndGet(), List.of()));
    }

    private NaturalPerson givenPerson(UUID workplaceId) {
        int sequence = SEQUENCE.incrementAndGet();
        return naturalPersonRepository.save(NaturalPerson.create("Pessoa " + sequence, "p" + sequence + "@example.com",
                null, "4299822" + String.format("%04d", sequence), null, null, null, null, null, workplaceId));
    }

    private Learner givenLearner(NaturalPerson person, UUID institutionId, UUID schoolGradeId) {
        return learnerRepository.save(Learner.create(person.getId(), institutionId, schoolGradeId, false, null,
                2026, null, null));
    }

    private FamilyGroup givenFamily(NaturalPerson... members) {
        int sequence = SEQUENCE.incrementAndGet();
        FamilyGroup family = familyGroupRepository.save(FamilyGroup.create("FAM-D" + sequence, "Família " + sequence,
                null, null, null, null, null, null, null));
        entityManager.flush();
        for (NaturalPerson member : members) {
            familyGroupRepository.linkNaturalPerson(family.getId(), member.getId());
        }
        return family;
    }

    private void markDeleted(String table, UUID id) {
        entityManager.flush();
        entityManager.createNativeQuery("update " + table + " set deleted_at = now() where id = :id")
                .setParameter("id", id).executeUpdate();
        entityManager.clear();
    }

    @Test
    @DisplayName("Pessoa física: conta educando, responsabilidade, colaborador e família ativos, ignorando os excluídos")
    void naturalPerson_ShouldCountOnlyActiveDependencies() {
        NaturalPerson person = givenPerson(null);
        givenLearner(person, null, null);
        Learner child = givenLearner(givenPerson(null), null, null);
        child.syncResponsibleList(List.of(Responsible.create(child, person.getId(), List.of(ResponsibleType.FINANCIAL), Kinship.FATHER, null)));
        learnerRepository.save(child);
        Learner deletedChild = givenLearner(givenPerson(null), null, null);
        deletedChild.syncResponsibleList(List.of(Responsible.create(deletedChild, person.getId(), List.of(ResponsibleType.FINANCIAL), Kinship.FATHER, null)));
        learnerRepository.save(deletedChild);
        collaboratorRepository.save(Collaborator.create(false, person.getId(), null));
        givenFamily(person);
        FamilyGroup deletedFamily = givenFamily(person);
        markDeleted("learner", deletedChild.getId());
        markDeleted("family_group", deletedFamily.getId());

        NaturalPersonRepository.NaturalPersonDependencies dependencies = naturalPersonRepository.countDependencies(person.getId());

        assertEquals(1, dependencies.getLearners());
        assertEquals(1, dependencies.getResponsibilities());
        assertEquals(1, dependencies.getCollaborators());
        assertEquals(1, dependencies.getFamilyGroups());
    }

    @Test
    @DisplayName("Pessoa física sem nenhum vínculo tem todas as contagens zeradas")
    void naturalPerson_ShouldReturnZerosWithoutDependencies() {
        NaturalPersonRepository.NaturalPersonDependencies dependencies =
                naturalPersonRepository.countDependencies(givenPerson(null).getId());

        assertEquals(0, dependencies.getLearners() + dependencies.getResponsibilities()
                + dependencies.getCollaborators() + dependencies.getFamilyGroups());
    }

    @Test
    @DisplayName("Pessoa jurídica: conta turmas, programas, colaboradores, educandos e locais de trabalho ativos")
    void legalPerson_ShouldCountOnlyActiveDependencies() {
        Program ownProgram = programRepository.save(
                Program.create("Programa da Escola " + SEQUENCE.incrementAndGet(), null, ProgramStatus.ACTIVE, institution.getId()));
        Program deletedProgram = programRepository.save(
                Program.create("Programa Excluído " + SEQUENCE.incrementAndGet(), null, ProgramStatus.ACTIVE, institution.getId()));
        collaboratorRepository.save(Collaborator.create(false, givenPerson(null).getId(), institution.getId()));
        givenLearner(givenPerson(null), institution.getId(), null);
        givenPerson(institution.getId());
        NaturalPerson deletedWorker = givenPerson(institution.getId());
        markDeleted("program", deletedProgram.getId());
        markDeleted("natural_person", deletedWorker.getId());

        LegalPersonRepository.LegalPersonDependencies dependencies = legalPersonRepository.countDependencies(institution.getId());

        assertEquals(1, dependencies.getClassGroups());
        assertEquals(1, dependencies.getPrograms(), "programa " + ownProgram.getName());
        assertEquals(1, dependencies.getCollaborators());
        assertEquals(1, dependencies.getLearners());
        assertEquals(1, dependencies.getWorkplaces());
    }

    @Test
    @DisplayName("Série escolar: conta educandos e responsáveis ativos, ignorando responsável de educando excluído")
    void schoolGrade_ShouldCountLearnersAndResponsibles() {
        SchoolGrade grade = schoolGradeRepository.save(SchoolGrade.create("Série " + SEQUENCE.incrementAndGet(), null, 1));
        Learner learner = givenLearner(givenPerson(null), null, grade.getId());
        learner.syncResponsibleList(List.of(Responsible.create(learner, givenPerson(null).getId(),
                List.of(ResponsibleType.FINANCIAL), Kinship.MOTHER, grade.getId())));
        learnerRepository.save(learner);
        Learner deletedLearner = givenLearner(givenPerson(null), null, grade.getId());
        deletedLearner.syncResponsibleList(List.of(Responsible.create(deletedLearner, givenPerson(null).getId(),
                List.of(ResponsibleType.FINANCIAL), Kinship.MOTHER, grade.getId())));
        learnerRepository.save(deletedLearner);
        markDeleted("learner", deletedLearner.getId());

        SchoolGradeRepository.SchoolGradeDependencies dependencies = schoolGradeRepository.countDependencies(grade.getId());

        assertEquals(0, dependencies.getClassGroups());
        assertEquals(1, dependencies.getLearners());
        assertEquals(1, dependencies.getResponsibles());
    }

    @Test
    @DisplayName("Colaborador: conta vínculos ativos com turmas, ignorando vínculo excluído e turma excluída")
    void collaborator_ShouldCountOnlyActiveClassLinks() {
        Collaborator collaborator = collaboratorRepository.save(Collaborator.create(false, givenPerson(null).getId(), null));
        UUID categoryId = program.getEducatorCategoryItemList().getFirst().getId();
        classCollaboratorRepository.save(ClassCollaborator.create(classGroup, collaborator.getId(), categoryId));
        ClassCollaborator removedLink = classCollaboratorRepository.save(
                ClassCollaborator.create(givenClassGroup(institution), collaborator.getId(), categoryId));
        ClassGroup deletedClassGroup = givenClassGroup(institution);
        classCollaboratorRepository.save(ClassCollaborator.create(deletedClassGroup, collaborator.getId(), categoryId));
        markDeleted("class_collaborator", removedLink.getId());
        markDeleted("class_group", deletedClassGroup.getId());

        assertEquals(1, collaboratorRepository.countActiveClassLinks(collaborator.getId()));
    }

    @Test
    @DisplayName("Turma: conta educandos (inclusive vínculo inativo) e educadores, ignorando vínculos excluídos")
    void classGroup_ShouldCountLearnerAndCollaboratorLinks() {
        classLearnerRepository.save(ClassLearner.create(classGroup, givenLearner(givenPerson(null), null, null).getId()));
        ClassLearner inactive = ClassLearner.create(classGroup, givenLearner(givenPerson(null), null, null).getId());
        inactive.deactivate();
        classLearnerRepository.save(inactive);
        ClassLearner removed = classLearnerRepository.save(
                ClassLearner.create(classGroup, givenLearner(givenPerson(null), null, null).getId()));
        Collaborator collaborator = collaboratorRepository.save(Collaborator.create(false, givenPerson(null).getId(), null));
        classCollaboratorRepository.save(ClassCollaborator.create(classGroup, collaborator.getId(),
                program.getEducatorCategoryItemList().getFirst().getId()));
        markDeleted("class_learner", removed.getId());

        ClassGroupRepository.ClassGroupDependencies dependencies = classGroupRepository.countDependencies(classGroup.getId());

        assertEquals(2, dependencies.getLearners());
        assertEquals(1, dependencies.getCollaborators());
    }

    @Test
    @DisplayName("Programa: conta só turmas não excluídas")
    void program_ShouldCountOnlyActiveClassGroups() {
        ClassGroup deletedClassGroup = givenClassGroup(institution);
        markDeleted("class_group", deletedClassGroup.getId());

        assertEquals(1, programRepository.countActiveClassGroups(program.getId()));
    }
}
