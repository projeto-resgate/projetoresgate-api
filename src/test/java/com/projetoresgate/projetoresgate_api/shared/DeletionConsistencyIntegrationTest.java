package com.projetoresgate.projetoresgate_api.shared;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRow;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRow;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Consistência após exclusões - Integração")
class DeletionConsistencyIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Autowired private ClassGroupRepository classGroupRepository;
    @Autowired private ClassLearnerRepository classLearnerRepository;
    @Autowired private ClassCollaboratorRepository classCollaboratorRepository;
    @Autowired private CollaboratorRepository collaboratorRepository;
    @Autowired private LearnerRepository learnerRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private LegalPersonRepository legalPersonRepository;
    @Autowired private NaturalPersonRepository naturalPersonRepository;
    @Autowired private FamilyGroupRepository familyGroupRepository;
    @Autowired private EntityManager entityManager;

    private ClassGroup classGroup;
    private Program program;

    @BeforeEach
    void setUp() {
        int sequence = SEQUENCE.incrementAndGet();
        program = Program.create("Programa Consistência " + sequence, null, ProgramStatus.ACTIVE, null);
        program.getEducatorCategoryItemList().add(EducatorCategoryItem.create("Professor", program));
        program = programRepository.save(program);
        LegalPerson institution = legalPersonRepository.save(givenLegalPerson(sequence, "Escola"));
        classGroup = classGroupRepository.save(ClassGroup.create(program.getId(), institution.getId(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Turma " + sequence, List.of()));
    }

    private LegalPerson givenLegalPerson(int sequence, String name) {
        Address address = Address.create("Rua", "Das Flores", "60000-000", "100", null, null, "Centro", "Fortaleza", "CE");
        return LegalPerson.create(String.format("%014d", 70000000000000L + sequence), name + " " + sequence, null,
                null, null, RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null);
    }

    private NaturalPerson givenPerson(String name, String email) {
        int sequence = SEQUENCE.incrementAndGet();
        return naturalPersonRepository.save(NaturalPerson.create(name, email, null,
                "4199822" + String.format("%04d", sequence), null, null, null, null, null));
    }

    private Learner givenLearner(NaturalPerson person) {
        return learnerRepository.save(Learner.create(person.getId(), null, null, false, null, 2026, null, null));
    }

    private FamilyGroup givenFamily(NaturalPerson... members) {
        int sequence = SEQUENCE.incrementAndGet();
        FamilyGroup family = familyGroupRepository.save(FamilyGroup.create("FAM-T" + sequence, "Família " + sequence,
                null, null, null, null, null, null, null));
        entityManager.flush();
        for (NaturalPerson member : members) {
            familyGroupRepository.linkNaturalPerson(family.getId(), member.getId());
        }
        return family;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Educando só pode sair da família se tiver outra família ativa")
    void isLearnerOnlyInFamilyGroup_ShouldConsiderOtherActiveFamilies() {
        NaturalPerson child = givenPerson("Criança", "crianca@example.com");
        givenLearner(child);
        FamilyGroup first = givenFamily(child);
        flushAndClear();

        assertTrue(familyGroupRepository.isLearnerOnlyInFamilyGroup(first.getId(), child.getId()));

        FamilyGroup second = givenFamily(child);
        flushAndClear();

        assertFalse(familyGroupRepository.isLearnerOnlyInFamilyGroup(first.getId(), child.getId()));

        familyGroupRepository.delete(familyGroupRepository.findByIdOrThrow(second.getId()));
        flushAndClear();

        assertTrue(familyGroupRepository.isLearnerOnlyInFamilyGroup(first.getId(), child.getId()));
    }

    @Test
    @DisplayName("Responsável por educando da família não pode sair dela")
    void isResponsibleForLearnerInFamilyGroup_ShouldDetectResponsible() {
        NaturalPerson child = givenPerson("Criança", "crianca2@example.com");
        NaturalPerson father = givenPerson("Pai", "pai@example.com");
        NaturalPerson uncle = givenPerson("Tio", "tio@example.com");
        Learner learner = givenLearner(child);
        learner.syncResponsibleList(List.of(
                Responsible.create(learner, father.getId(), ResponsibleType.FINANCIAL, Kinship.FATHER, null)));
        learnerRepository.save(learner);
        FamilyGroup family = givenFamily(child, father, uncle);
        flushAndClear();

        assertTrue(familyGroupRepository.isResponsibleForLearnerInFamilyGroup(family.getId(), father.getId()));
        assertFalse(familyGroupRepository.isResponsibleForLearnerInFamilyGroup(family.getId(), uncle.getId()));
    }

    @Test
    @DisplayName("CPF, RG e CNPJ de registros excluídos podem ser cadastrados de novo")
    void uniqueKeys_ShouldIgnoreDeletedRows() {
        NaturalPerson person = naturalPersonRepository.save(NaturalPerson.create("Primeira", "a@example.com", null,
                "52998224725", "1234567", null, null, null, null));
        LegalPerson company = legalPersonRepository.save(givenLegalPerson(SEQUENCE.incrementAndGet(), "Empresa"));
        flushAndClear();

        naturalPersonRepository.delete(naturalPersonRepository.findByIdOrThrow(person.getId()));
        legalPersonRepository.delete(legalPersonRepository.findByIdOrThrow(company.getId()));
        flushAndClear();

        assertDoesNotThrow(() -> {
            naturalPersonRepository.save(NaturalPerson.create("Segunda", "b@example.com", null,
                    "52998224725", "1234567", null, null, null, null));
            Address address = Address.create(null, null, "60000-000", null, null, null, null, "Fortaleza", "CE");
            legalPersonRepository.save(LegalPerson.create(company.getCnpj(), "Empresa nova", null, null, null,
                    RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Pessoas podem compartilhar o mesmo e-mail e a busca ignora maiúsculas")
    void findAllByEmailIgnoreCase_ShouldReturnEveryPersonSharingTheEmail() {
        givenPerson("Mãe", "familia@example.com");
        givenPerson("Filho", "FAMILIA@example.com");
        flushAndClear();

        assertEquals(2, naturalPersonRepository.findAllByEmailIgnoreCase("Familia@Example.com").size());
    }
}
