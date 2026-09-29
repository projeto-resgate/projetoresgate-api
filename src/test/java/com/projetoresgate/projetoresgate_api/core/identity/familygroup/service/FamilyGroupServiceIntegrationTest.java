package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupNaturalPersonResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.*;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindAllFamilyGroupsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("FamilyGroupService - Integração")
class FamilyGroupServiceIntegrationTest extends PostgresIntegrationTest {

    private static final Pageable PAGE_ALL = PageRequest.of(0, 50, Sort.by("name").ascending());

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private CreateFamilyGroupService createService;
    private UpdateFamilyGroupService updateService;
    private FindAllFamilyGroupsService searchService;
    private FindFamilyGroupNaturalPersonsService findNaturalPersonsService;
    private AddNaturalPersonToFamilyGroupService addNaturalPersonService;
    private RemoveNaturalPersonFromFamilyGroupService removeNaturalPersonService;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("ALTER SEQUENCE family_group_friendly_id_seq RESTART WITH 1").executeUpdate();

        createService = new CreateFamilyGroupService(familyGroupRepository);
        updateService = new UpdateFamilyGroupService(familyGroupRepository);
        searchService = new FindAllFamilyGroupsService(familyGroupRepository);
        findNaturalPersonsService = new FindFamilyGroupNaturalPersonsService(familyGroupRepository);
        addNaturalPersonService = new AddNaturalPersonToFamilyGroupService(
                familyGroupRepository, naturalPersonRepository);
        removeNaturalPersonService = new RemoveNaturalPersonFromFamilyGroupService(familyGroupRepository);
    }

    private NaturalPerson givenPerson(String name, String cpf) {
        return naturalPersonRepository.save(NaturalPerson.create(
                name, name.toLowerCase() + "@email.com", null, cpf, null, null, null, null, null));
    }

    private CreateFamilyGroupCommand buildCommand(String name, String city) {
        return new CreateFamilyGroupCommand(
                name,
                new BigDecimal("5000.00"),
                new BigDecimal("1250.00"),
                new BigDecimal("800.00"),
                new BigDecimal("400.00"),
                new BigDecimal("1500.00"),
                4,
                new AddressCommand("Rua", "das Palmeiras", "01310-100", "1000", "Próximo à praça",
                        "Apto 101", "Bela Vista", city, "SP"));
    }

    private UpdateFamilyGroupCommand buildUpdateCommand(UUID id, String name) {
        return new UpdateFamilyGroupCommand(
                id, name, new BigDecimal("6000.00"), new BigDecimal("1500.00"),
                new BigDecimal("900.00"), new BigDecimal("500.00"), new BigDecimal("1600.00"), 5,
                new AddressCommand("Avenida", "Paulista", "01310-100", "1000", "Em frente ao museu",
                        "Apto 202", "Bela Vista", "São Paulo", "SP"));
    }

    @Test
    @DisplayName("Deve persistir o endereço na tabela separada e montar a resposta com o endereço completo")
    void handle_ShouldPersistAddressInSeparateTable() {
        FamilyGroup saved = createService.handle(buildCommand("Família Silva", "São Paulo"));
        familyGroupRepository.flush();

        FamilyGroup found = familyGroupRepository.findByIdOrThrow(saved.getId());

        assertNotNull(found.getAddress());
        assertNotNull(found.getAddress().getId());
        assertEquals("São Paulo", found.getAddress().getCity());
        assertEquals(4, found.getNumberOfResidents());

        FamilyGroupResponse response = FamilyGroupResponse.fromEntity(found);

        assertNotNull(response.address());
        assertEquals(found.getAddress().getId(), response.address().id());
        assertEquals("Apto 101", response.address().complement());
        assertEquals(4, response.numberOfResidents());
    }

    @Test
    @DisplayName("Deve criar grupo familiar sem endereço")
    void handle_ShouldPersistWithoutAddress() {
        CreateFamilyGroupCommand command = new CreateFamilyGroupCommand(
                "Família Souza", null, null, null, null, null, null, null);

        FamilyGroup saved = createService.handle(command);
        familyGroupRepository.flush();

        FamilyGroup found = familyGroupRepository.findByIdOrThrow(saved.getId());

        assertNull(found.getAddress());
        assertNull(FamilyGroupResponse.fromEntity(found).address());
    }

    @Test
    @DisplayName("Deve atualizar os dados e reaproveitar o endereço já persistido")
    void handle_ShouldUpdateDataAndKeepSingleAddress() {
        FamilyGroup saved = createService.handle(buildCommand("Família Silva", "São Paulo"));
        familyGroupRepository.flush();
        UUID addressIdBefore = saved.getAddress().getId();

        updateService.handle(buildUpdateCommand(saved.getId(), "Família Silva Souza"));
        familyGroupRepository.flush();

        FamilyGroup found = familyGroupRepository.findByIdOrThrow(saved.getId());

        assertEquals("Família Silva Souza", found.getName());
        assertEquals(new BigDecimal("6000.00"), found.getHouseholdIncome());
        assertEquals(5, found.getNumberOfResidents());
        assertEquals(addressIdBefore, found.getAddress().getId());
        assertEquals("Apto 202", found.getAddress().getComplement());
        assertEquals("Avenida", found.getAddress().getStreetType());
        assertEquals("Paulista", found.getAddress().getStreetName());
        assertEquals("Em frente ao museu", found.getAddress().getReferencePoint());
    }

    @Test
    @DisplayName("Deve gerar um friendlyId sequencial no padrão FAM-<número> para cada grupo familiar")
    void handle_ShouldGenerateSequentialUniqueFriendlyId() {
        FamilyGroup first = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        FamilyGroup second = createService.handle(buildCommand("Família Beta", "Curitiba"));
        familyGroupRepository.flush();

        FamilyGroup reloadedFirst = familyGroupRepository.findByIdOrThrow(first.getId());
        FamilyGroup reloadedSecond = familyGroupRepository.findByIdOrThrow(second.getId());

        assertEquals("FAM-1", reloadedFirst.getFriendlyId());
        assertEquals("FAM-2", reloadedSecond.getFriendlyId());
    }

    @Test
    @DisplayName("Deve retornar na listagem a quantidade de pessoas cadastradas em cada grupo")
    void handle_ShouldReturnRegisteredPeopleCountPerGroup() {
        NaturalPerson maria = naturalPersonRepository.save(
                NaturalPerson.create("Maria Silva", "maria@email.com", "Maria", "52998224725", "123456789",
                        null, null, null, null));
        NaturalPerson joao = naturalPersonRepository.save(
                NaturalPerson.create("João Silva", "joao@email.com", "João", "11144477735", "987654321",
                        null, null, null, null));

        FamilyGroup withPeople = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        FamilyGroup withoutPeople = createService.handle(buildCommand("Família Beta", "Curitiba"));

        familyGroupRepository.findByIdOrThrow(withPeople.getId())
                .update()
                .naturalPersonList(List.of(maria, joao))
                .apply();
        familyGroupRepository.flush();

        Page<FamilyGroupSummaryResponse> result = searchService.handle(
                new FindAllFamilyGroupsQuery(null, PageRequest.of(0, 10)));

        assertEquals(2, result.getTotalElements());

        FamilyGroupSummaryResponse alfa = result.getContent().stream()
                .filter(summary -> summary.id().equals(withPeople.getId()))
                .findFirst()
                .orElseThrow();
        FamilyGroupSummaryResponse beta = result.getContent().stream()
                .filter(summary -> summary.id().equals(withoutPeople.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(2L, alfa.registeredPeopleCount());
        assertEquals(0L, beta.registeredPeopleCount());
        assertNotNull(alfa.friendlyId());
    }

    @Test
    @DisplayName("Deve excluir o grupo familiar de forma lógica")
    void handle_ShouldSoftDeleteFamilyGroup() {
        FamilyGroup saved = createService.handle(buildCommand("Família Silva", "São Paulo"));
        familyGroupRepository.flush();

        new SoftDeleteFamilyGroupService(familyGroupRepository)
                .handle(new SoftDeleteFamilyGroupCommand(saved.getId()));
        familyGroupRepository.flush();
        entityManager.clear();

        assertTrue(familyGroupRepository.findById(saved.getId()).isEmpty());
    }

    @Test
    @DisplayName("Deve persistir e carregar a lista de pessoas físicas vinculadas")
    void handle_ShouldPersistLinkedNaturalPersons() {
        NaturalPerson person = naturalPersonRepository.save(
                NaturalPerson.create("Maria Silva", "maria@email.com", "Maria", "52998224725", "123456789",
                        null, null, null, null));

        FamilyGroup saved = createService.handle(buildCommand("Família Silva", "São Paulo"));

        familyGroupRepository.findByIdOrThrow(saved.getId())
                .update()
                .naturalPersonList(List.of(person))
                .apply();
        familyGroupRepository.flush();

        FamilyGroup found = familyGroupRepository.findByIdOrThrow(saved.getId());

        assertEquals(1, found.getNaturalPersonList().size());
        assertEquals("Maria Silva", found.getNaturalPersonList().getFirst().getName());
    }

    @Test
    @DisplayName("Deve retornar as pessoas físicas vinculadas ao grupo familiar")
    void handle_ShouldReturnLinkedNaturalPersons() {
        NaturalPerson daniela = naturalPersonRepository.save(
                NaturalPerson.create("Daniela Ferreira", "daniela@email.com", null, "11144477735", "123456789",
                        null, null, null, "11911112222"));
        NaturalPerson breno = naturalPersonRepository.save(
                NaturalPerson.create("Breno Ferreira", "breno@email.com", null, "11144477736", "223456789",
                        null, null, null, "11911113333"));

        FamilyGroup saved = createService.handle(buildCommand("Família Ferreira", "Joinville"));

        familyGroupRepository.findByIdOrThrow(saved.getId())
                .update()
                .naturalPersonList(List.of(daniela, breno))
                .apply();
        familyGroupRepository.flush();

        Page<FamilyGroupNaturalPersonResponse> result = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(saved.getId(), PAGE_ALL));

        assertEquals(2, result.getTotalElements());
        assertEquals("Daniela Ferreira", result.getContent().getFirst().name());
        assertEquals("11144477735", result.getContent().getFirst().cpf());
        assertEquals("11911112222", result.getContent().getFirst().cellphone());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o grupo familiar não tiver pessoas vinculadas")
    void handle_ShouldReturnEmptyListWhenNoLinkedPersons() {
        FamilyGroup saved = createService.handle(buildCommand("Família Ferreira", "Joinville"));
        familyGroupRepository.flush();

        Page<FamilyGroupNaturalPersonResponse> result = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(saved.getId(), PAGE_ALL));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve paginar as pessoas vinculadas, preservando o total")
    void handle_ShouldPaginateLinkedNaturalPersons() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        NaturalPerson breno = givenPerson("Breno Ferreira", "11144477735");
        NaturalPerson daniela = givenPerson("Daniela Ferreira", "11144477736");
        NaturalPerson maria = givenPerson("Maria Silva", "11144477737");
        entityManager.flush();

        for (NaturalPerson person : List.of(breno, daniela, maria)) {
            addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId()));
        }
        entityManager.flush();
        entityManager.clear();

        Page<FamilyGroupNaturalPersonResponse> firstPage = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(group.getId(), PageRequest.of(0, 2, Sort.by("name").ascending())));
        Page<FamilyGroupNaturalPersonResponse> secondPage = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(group.getId(), PageRequest.of(1, 2, Sort.by("name").ascending())));

        assertEquals(2, firstPage.getContent().size());
        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals("Breno Ferreira", firstPage.getContent().getFirst().name());
        assertEquals("Daniela Ferreira", firstPage.getContent().get(1).name());

        assertEquals(1, secondPage.getContent().size());
        assertEquals(3, secondPage.getTotalElements());
        assertEquals("Maria Silva", secondPage.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Deve retornar página vazia com total zero para grupo sem pessoas vinculadas")
    void handle_ShouldReturnZeroTotalForGroupWithoutLinkedPersons() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        familyGroupRepository.flush();

        Page<FamilyGroupNaturalPersonResponse> result = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(group.getId(), PageRequest.of(0, 10, Sort.by("name").ascending())));

        // Um left join devolveria uma linha com null aqui, o que faria a paginacao reportar
        // 1 elemento para um grupo sem ninguem vinculado.
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    @DisplayName("Deve paginar apenas as pessoas do grupo solicitado, sem vazar de outros grupos")
    void handle_ShouldNotLeakPeopleFromOtherGroups() {
        FamilyGroup first = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        FamilyGroup second = createService.handle(buildCommand("Família Beta", "Curitiba"));
        NaturalPerson maria = givenPerson("Maria Silva", "11144477735");
        NaturalPerson joao = givenPerson("João Souza", "11144477736");
        entityManager.flush();

        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(first.getId(), maria.getId()));
        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(second.getId(), joao.getId()));
        entityManager.flush();
        entityManager.clear();

        Page<FamilyGroupNaturalPersonResponse> result = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(first.getId(), PageRequest.of(0, 10, Sort.by("name").ascending())));

        assertEquals(1, result.getTotalElements());
        assertEquals("Maria Silva", result.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Deve filtrar por nome")
    void handle_ShouldFilterByName() {
        createService.handle(buildCommand("Família Silva", "São Paulo"));
        createService.handle(buildCommand("Família Souza", "Rio de Janeiro"));
        familyGroupRepository.flush();

        Page<FamilyGroupSummaryResponse> result = searchService.handle(
                new FindAllFamilyGroupsQuery("silva", PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Família Silva", result.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Deve vincular pessoa fisica existente ao grupo e retornar na listagem")
    void handle_ShouldLinkExistingNaturalPerson() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        entityManager.flush();

        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId()));
        entityManager.flush();
        entityManager.clear();

        Page<FamilyGroupNaturalPersonResponse> linked = findNaturalPersonsService
                .handle(new FindFamilyGroupNaturalPersonsQuery(group.getId(), PAGE_ALL));

        assertEquals(1, linked.getTotalElements());
        assertEquals("Maria Silva", linked.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Deve desvincular pessoa fisica e preservar o cadastro")
    void handle_ShouldUnlinkKeepingThePerson() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId()));
        entityManager.flush();

        removeNaturalPersonService.handle(
                new RemoveNaturalPersonFromFamilyGroupCommand(group.getId(), person.getId()));
        entityManager.flush();
        entityManager.clear();

        assertTrue(findNaturalPersonsService
                .handle(new FindFamilyGroupNaturalPersonsQuery(group.getId(), PAGE_ALL)).getContent().isEmpty());
        assertTrue(naturalPersonRepository.findById(person.getId()).isPresent());
    }

    @Test
    @DisplayName("Deve rejeitar o vinculo duplicado da mesma pessoa no mesmo grupo")
    void handle_ShouldRejectDuplicateLink() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId()));
        entityManager.flush();

        assertThrows(IllegalStateException.class,
                () -> addNaturalPersonService.handle(
                        new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId())));
    }

    @Test
    @DisplayName("Deve permitir a mesma pessoa em grupos diferentes")
    void handle_ShouldAllowSamePersonInDifferentGroups() {
        FamilyGroup first = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        FamilyGroup second = createService.handle(buildCommand("Família Beta", "Curitiba"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        entityManager.flush();

        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(first.getId(), person.getId()));
        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(second.getId(), person.getId()));
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, findNaturalPersonsService
                .handle(new FindFamilyGroupNaturalPersonsQuery(first.getId(), PAGE_ALL)).getTotalElements());
        assertEquals(1, findNaturalPersonsService
                .handle(new FindFamilyGroupNaturalPersonsQuery(second.getId(), PAGE_ALL)).getTotalElements());
    }

    @Test
    @DisplayName("Nao deve alterar o numberOfResidents ao vincular ou desvincular")
    void handle_ShouldNotChangeNumberOfResidents() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        entityManager.flush();

        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId()));
        entityManager.flush();
        entityManager.clear();
        assertEquals(4, familyGroupRepository.findByIdOrThrow(group.getId()).getNumberOfResidents());

        removeNaturalPersonService.handle(
                new RemoveNaturalPersonFromFamilyGroupCommand(group.getId(), person.getId()));
        entityManager.flush();
        entityManager.clear();
        assertEquals(4, familyGroupRepository.findByIdOrThrow(group.getId()).getNumberOfResidents());
    }

    @Test
    @DisplayName("Nao deve vincular pessoa que foi deletada")
    void handle_ShouldNotLinkSoftDeletedPerson() {
        FamilyGroup group = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        entityManager.flush();

        naturalPersonRepository.delete(person);
        entityManager.flush();
        entityManager.clear();

        assertThrows(ResourceNotFoundException.class,
                () -> addNaturalPersonService.handle(
                        new AddNaturalPersonToFamilyGroupCommand(group.getId(), person.getId())));
    }

    @Test
    @DisplayName("Deve rejeitar desvinculo de pessoa que nao pertence ao grupo")
    void handle_ShouldRejectUnlinkWhenNotLinked() {
        FamilyGroup first = createService.handle(buildCommand("Família Alfa", "São Paulo"));
        FamilyGroup second = createService.handle(buildCommand("Família Beta", "Curitiba"));
        NaturalPerson person = givenPerson("Maria Silva", "11144477735");
        addNaturalPersonService.handle(new AddNaturalPersonToFamilyGroupCommand(first.getId(), person.getId()));
        entityManager.flush();

        assertThrows(IllegalStateException.class,
                () -> removeNaturalPersonService.handle(
                        new RemoveNaturalPersonFromFamilyGroupCommand(second.getId(), person.getId())));
    }
}
