package com.projetoresgate.projetoresgate_api.shared;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.service.FindAllClassCollaboratorsService;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.service.FindAllClassGroupsService;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.service.FindAllClassLearnersService;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.service.AutocompleteCollaboratorService;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.service.FindAllCollaboratorsService;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.AutocompleteCollaboratorQuery;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.service.AutocompleteLearnerService;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.AutocompleteLearnerQuery;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.ProgramRepository;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
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
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Consultas de listagem com SQL nativo - Integração")
class ListingQueriesIntegrationTest extends PostgresIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Autowired private CollaboratorRepository collaboratorRepository;
    @Autowired private ClassGroupRepository classGroupRepository;
    @Autowired private ClassLearnerRepository classLearnerRepository;
    @Autowired private ClassCollaboratorRepository classCollaboratorRepository;
    @Autowired private LearnerRepository learnerRepository;
    @Autowired private ProgramRepository programRepository;
    @Autowired private LegalPersonRepository legalPersonRepository;
    @Autowired private NaturalPersonRepository naturalPersonRepository;
    @Autowired private EntityManager entityManager;

    private FindAllCollaboratorsService findAllCollaborators;
    private AutocompleteCollaboratorService autocompleteCollaborators;
    private FindAllClassLearnersService findAllClassLearners;
    private FindAllClassCollaboratorsService findAllClassCollaborators;
    private FindAllClassGroupsService findAllClassGroups;
    private AutocompleteLearnerService autocompleteLearners;

    private String tag;

    @BeforeEach
    void setUp() {
        findAllCollaborators = new FindAllCollaboratorsService(collaboratorRepository);
        autocompleteCollaborators = new AutocompleteCollaboratorService(collaboratorRepository);
        findAllClassLearners = new FindAllClassLearnersService(classLearnerRepository, classGroupRepository);
        findAllClassCollaborators = new FindAllClassCollaboratorsService(classCollaboratorRepository, classGroupRepository);
        findAllClassGroups = new FindAllClassGroupsService(classGroupRepository);
        autocompleteLearners = new AutocompleteLearnerService(learnerRepository);
        tag = "Lq" + SEQUENCE.incrementAndGet() + "x";
    }

    private NaturalPerson givenPerson(String name, String cpf, String rg) {
        return naturalPersonRepository.save(NaturalPerson.create(name + " " + tag,
                tag.toLowerCase() + SEQUENCE.incrementAndGet() + "@example.com", null, cpf, rg, null, null, null, null));
    }

    private LegalPerson givenLegalPerson(String corporateName, String tradeName, String displayName) {
        int sequence = SEQUENCE.incrementAndGet();
        Address address = Address.create(null, null, "60000-000", null, null, null, null, "Fortaleza", "CE");
        return legalPersonRepository.save(LegalPerson.create(String.format("%014d", 80000000000000L + sequence),
                corporateName + " " + tag, tradeName, displayName, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, address, null));
    }

    private Program givenProgram(String name, String... categories) {
        Program program = Program.create(name + " " + tag, null, ProgramStatus.ACTIVE, null);
        for (String category : categories) {
            program.getEducatorCategoryItemList().add(EducatorCategoryItem.create(category, program));
        }
        return programRepository.save(program);
    }

    private ClassGroup givenClassGroup(Program program, LegalPerson institution) {
        return classGroupRepository.save(ClassGroup.create(program.getId(), institution.getId(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, program.getName(), List.of()));
    }

    private Learner givenLearner(NaturalPerson person) {
        return learnerRepository.save(Learner.create(person.getId(), null, null, false, null, 2026, null, null));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Lista de colaboradores traz nomes, cai para a razão social e busca por nome, CPF, RG e CNPJ")
    void collaborators_ShouldReturnNamesAndSearchByDocuments() {
        LegalPerson company = givenLegalPerson("Empresa Razão", null, null);
        NaturalPerson ana = givenPerson("Ana Souza", "93089835207", "MG98765431");
        NaturalPerson bruno = givenPerson("Bruno Lima", "82643823397", null);
        collaboratorRepository.save(Collaborator.create(true, ana.getId(), company.getId()));
        collaboratorRepository.save(Collaborator.create(false, bruno.getId(), null));
        flushAndClear();

        Page<CollaboratorSummaryResponse> all = findAllCollaborators.handle(
                new FindAllCollaboratorsQuery(tag, PageRequest.of(0, 10)));

        assertEquals(List.of("Ana Souza " + tag, "Bruno Lima " + tag),
                all.getContent().stream().map(CollaboratorSummaryResponse::naturalPersonName).toList());
        assertEquals("Empresa Razão " + tag, all.getContent().getFirst().legalPersonName());
        assertTrue(all.getContent().getFirst().isVoluntary());
        assertNull(all.getContent().get(1).legalPersonName());

        assertEquals(1, findAllCollaborators.handle(new FindAllCollaboratorsQuery("93089835207", PageRequest.of(0, 10))).getTotalElements());
        assertEquals(1, findAllCollaborators.handle(new FindAllCollaboratorsQuery("mg98765431", PageRequest.of(0, 10))).getTotalElements());
        assertEquals(1, findAllCollaborators.handle(new FindAllCollaboratorsQuery(company.getCnpj(), PageRequest.of(0, 10))).getTotalElements());
        assertEquals(1, findAllCollaborators.handle(new FindAllCollaboratorsQuery("razão " + tag, PageRequest.of(0, 10))).getTotalElements());
    }

    @Test
    @DisplayName("Lista de colaboradores mostra quem teve pessoa ou empresa excluída; o autocomplete esconde pessoa excluída")
    void collaborators_ShouldFollowDeletionPolicy() {
        LegalPerson company = givenLegalPerson("Empresa Excluída", "Fantasia Excluída", null);
        NaturalPerson ana = givenPerson("Ana Souza", null, null);
        NaturalPerson bruno = givenPerson("Bruno Lima", null, null);
        NaturalPerson carla = givenPerson("Carla Dias", null, null);
        collaboratorRepository.save(Collaborator.create(false, ana.getId(), company.getId()));
        collaboratorRepository.save(Collaborator.create(false, bruno.getId(), null));
        Collaborator carlaCollaborator = collaboratorRepository.save(Collaborator.create(false, carla.getId(), null));
        flushAndClear();

        legalPersonRepository.delete(legalPersonRepository.findByIdOrThrow(company.getId()));
        naturalPersonRepository.delete(naturalPersonRepository.findByIdOrThrow(bruno.getId()));
        collaboratorRepository.delete(collaboratorRepository.findByIdOrThrow(carlaCollaborator.getId()));
        flushAndClear();

        List<CollaboratorSummaryResponse> list = findAllCollaborators.handle(
                new FindAllCollaboratorsQuery(tag, PageRequest.of(0, 10))).getContent();
        List<CollaboratorAutocompleteResponse> suggestions = autocompleteCollaborators.handle(
                new AutocompleteCollaboratorQuery(tag, 10));

        assertEquals(List.of("Ana Souza " + tag, "Bruno Lima " + tag),
                list.stream().map(CollaboratorSummaryResponse::naturalPersonName).toList());
        assertEquals("Fantasia Excluída", list.getFirst().legalPersonName());
        assertEquals(List.of("Ana Souza " + tag),
                suggestions.stream().map(CollaboratorAutocompleteResponse::naturalPersonName).toList());
    }

    @Test
    @DisplayName("Autocomplete de colaborador respeita o limite")
    void collaboratorAutocomplete_ShouldRespectLimit() {
        for (String name : List.of("Ana", "Bruno", "Carla")) {
            collaboratorRepository.save(Collaborator.create(false, givenPerson(name, null, null).getId(), null));
        }
        flushAndClear();

        assertEquals(2, autocompleteCollaborators.handle(new AutocompleteCollaboratorQuery(tag, 2)).size());
    }

    @Test
    @DisplayName("Educandos da turma trazem nome, CPF, RG e status, com ativos primeiro e pessoa excluída mantida")
    void classLearners_ShouldReturnRowsWithPersonData() {
        ClassGroup classGroup = givenClassGroup(givenProgram("Programa"), givenLegalPerson("Escola", null, "Escola"));
        NaturalPerson ana = givenPerson("Ana Souza", "93089835207", "MG98765431");
        NaturalPerson bruno = givenPerson("Bruno Lima", null, null);
        Learner anaLearner = givenLearner(ana);
        Learner brunoLearner = givenLearner(bruno);
        ClassLearner inactive = ClassLearner.create(classGroup, anaLearner.getId());
        inactive.deactivate();
        classLearnerRepository.save(inactive);
        classLearnerRepository.save(ClassLearner.create(classGroup, brunoLearner.getId()));
        flushAndClear();

        naturalPersonRepository.delete(naturalPersonRepository.findByIdOrThrow(ana.getId()));
        flushAndClear();

        List<ClassLearnerResponse> rows = findAllClassLearners.handle(new FindAllClassLearnersQuery(
                classGroup.getId(), null, null, PageRequest.of(0, 10))).getContent();

        assertEquals(List.of(brunoLearner.getId(), anaLearner.getId()), rows.stream().map(ClassLearnerResponse::learnerId).toList());
        ClassLearnerResponse anaRow = rows.get(1);
        assertEquals("Ana Souza " + tag, anaRow.learnerName());
        assertEquals("93089835207", anaRow.learnerCpf());
        assertEquals("MG98765431", anaRow.learnerRg());
        // RG e CPF são opcionais: quem não tem continua na lista, com os campos nulos
        assertNull(rows.getFirst().learnerCpf());
        assertNull(rows.getFirst().learnerRg());
        assertEquals(LearnerStatus.ACTIVE, anaRow.learnerStatus());
        assertFalse(anaRow.active());
        assertEquals(classGroup.getId(), anaRow.classGroupId());
        assertNotNull(anaRow.dateCreated());
        assertEquals(1, findAllClassLearners.handle(new FindAllClassLearnersQuery(
                classGroup.getId(), "bruno", true, PageRequest.of(0, 10))).getTotalElements());
    }

    @Test
    @DisplayName("Educadores da turma trazem nome, CPF, RG e categoria, inclusive de categoria excluída")
    void classCollaborators_ShouldReturnNamesAndCategory() {
        Program program = givenProgram("Programa", "Professor", "Auxiliar");
        ClassGroup classGroup = givenClassGroup(program, givenLegalPerson("Escola", null, "Escola"));
        EducatorCategoryItem professor = program.getEducatorCategoryItemList().getFirst();
        EducatorCategoryItem helper = program.getEducatorCategoryItemList().get(1);
        Collaborator ana = collaboratorRepository.save(Collaborator.create(false, givenPerson("Ana Souza", "52998224725", "SP1234567").getId(), null));
        Collaborator bruno = collaboratorRepository.save(Collaborator.create(false, givenPerson("Bruno Lima", null, null).getId(), null));
        classCollaboratorRepository.save(ClassCollaborator.create(classGroup, ana.getId(), professor.getId()));
        classCollaboratorRepository.save(ClassCollaborator.create(classGroup, bruno.getId(), helper.getId()));
        flushAndClear();

        entityManager.createNativeQuery("update educator_category_item set deleted_at = now() where id = :id")
                .setParameter("id", helper.getId()).executeUpdate();
        flushAndClear();

        List<ClassCollaboratorResponse> rows = findAllClassCollaborators.handle(new FindAllClassCollaboratorsQuery(
                classGroup.getId(), null, null, null, PageRequest.of(0, 10))).getContent();

        assertEquals(List.of("Ana Souza " + tag, "Bruno Lima " + tag),
                rows.stream().map(ClassCollaboratorResponse::collaboratorName).toList());
        assertEquals(List.of("Professor", "Auxiliar"),
                rows.stream().map(ClassCollaboratorResponse::educatorCategoryItemName).toList());
        assertEquals("52998224725", rows.getFirst().collaboratorCpf());
        assertEquals("SP1234567", rows.getFirst().collaboratorRg());
        assertNull(rows.get(1).collaboratorCpf());
        assertNull(rows.get(1).collaboratorRg());
        assertEquals(1, findAllClassCollaborators.handle(new FindAllClassCollaboratorsQuery(
                classGroup.getId(), professor.getId(), "ana", true, PageRequest.of(0, 10))).getTotalElements());
    }

    @Test
    @DisplayName("Lista de turmas traz nomes de programa e instituição (com fallback e excluídos), filtra e ordena por título")
    void classGroups_ShouldReturnNamesAndFilter() {
        Program alpha = givenProgram("Alfa");
        Program beta = givenProgram("Beta");
        LegalPerson school = givenLegalPerson("Escola Razão", "Escola Fantasia", null);
        ClassGroup betaGroup = givenClassGroup(beta, school);
        ClassGroup alphaGroup = givenClassGroup(alpha, school);
        ClassGroup inactive = givenClassGroup(alpha, school);
        inactive.update().active(false).apply();
        classGroupRepository.save(inactive);
        flushAndClear();

        programRepository.delete(programRepository.findByIdOrThrow(beta.getId()));
        legalPersonRepository.delete(legalPersonRepository.findByIdOrThrow(school.getId()));
        flushAndClear();

        List<ClassGroupSummaryResponse> all = findAllClassGroups.handle(new FindAllClassGroupsQuery(
                tag, null, null, null, PageRequest.of(0, 10, Sort.by("title")))).getContent();

        assertEquals(3, all.size());
        assertEquals(List.of(alphaGroup.getTitle(), alphaGroup.getTitle(), betaGroup.getTitle()),
                all.stream().map(ClassGroupSummaryResponse::title).toList());
        ClassGroupSummaryResponse betaRow = all.get(2);
        assertEquals("Beta " + tag, betaRow.programName());
        assertEquals("Escola Fantasia", betaRow.institutionName());
        assertEquals(2, findAllClassGroups.handle(new FindAllClassGroupsQuery(
                null, alpha.getId(), school.getId(), null, PageRequest.of(0, 10))).getTotalElements());
        assertEquals(1, findAllClassGroups.handle(new FindAllClassGroupsQuery(
                null, alpha.getId(), null, false, PageRequest.of(0, 10))).getTotalElements());
    }

    @Test
    @DisplayName("Autocomplete de educando esconde pessoa excluída sem perder itens do limite e busca por CPF")
    void learnerAutocomplete_ShouldApplyLimitAfterFiltering() {
        NaturalPerson deleted = givenPerson("Ana Excluída", null, null);
        givenLearner(givenPerson("Bruno Lima", "19868487528", null));
        givenLearner(givenPerson("Carla Dias", null, null));
        givenLearner(deleted);
        flushAndClear();

        naturalPersonRepository.delete(naturalPersonRepository.findByIdOrThrow(deleted.getId()));
        flushAndClear();

        List<LearnerAutocompleteResponse> suggestions = autocompleteLearners.handle(new AutocompleteLearnerQuery(tag, 2));
        List<LearnerAutocompleteResponse> byCpf = autocompleteLearners.handle(new AutocompleteLearnerQuery("19868487528", 10));
        List<LearnerAutocompleteResponse> byName = autocompleteLearners.handle(new AutocompleteLearnerQuery("Carla Dias " + tag, 10));

        assertEquals(2, suggestions.size());
        assertTrue(suggestions.stream().noneMatch(suggestion -> suggestion.name().startsWith("Ana")));
        assertEquals(1, byCpf.size());
        assertEquals("19868487528", byCpf.getFirst().cpf());
        assertTrue(byCpf.getFirst().active());
        assertEquals(1, byName.size());
    }

    @Test
    @DisplayName("Autocomplete de educando não traz desistentes, sem perder itens do limite")
    void learnerAutocomplete_ShouldHideDroppedOutLearners() {
        Learner dropped = givenLearner(givenPerson("Ana Desistente", null, null));
        givenLearner(givenPerson("Bruno Lima", null, null));
        givenLearner(givenPerson("Carla Dias", null, null));
        dropped.dropOut("Mudou de cidade", null);
        learnerRepository.save(dropped);
        flushAndClear();

        List<LearnerAutocompleteResponse> suggestions = autocompleteLearners.handle(new AutocompleteLearnerQuery(tag, 2));
        List<LearnerAutocompleteResponse> byName = autocompleteLearners.handle(
                new AutocompleteLearnerQuery("Ana Desistente " + tag, 10));

        assertEquals(2, suggestions.size());
        assertTrue(suggestions.stream().noneMatch(suggestion -> suggestion.name().startsWith("Ana")));
        assertTrue(suggestions.stream().allMatch(LearnerAutocompleteResponse::active));
        assertTrue(byName.isEmpty());
    }
}
