package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupSequenceRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.SoftDeleteFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindFamilyGroupNaturalPersonsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.SearchFamilyGroupQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("FamilyGroupService - Integração")
class FamilyGroupServiceIntegrationTest {

    @Autowired
    private FamilyGroupRepository familyGroupRepository;

    @Autowired
    private NaturalPersonRepository naturalPersonRepository;

    @Autowired
    private FamilyGroupSequenceRepository familyGroupSequenceRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private CreateFamilyGroupService createService;
    private UpdateFamilyGroupService updateService;
    private SearchFamilyGroupService searchService;
    private FindFamilyGroupNaturalPersonsService findNaturalPersonsService;

    @BeforeEach
    void setUp() {
        createService = new CreateFamilyGroupService(familyGroupRepository, familyGroupSequenceRepository);
        updateService = new UpdateFamilyGroupService(familyGroupRepository);
        searchService = new SearchFamilyGroupService(familyGroupRepository);
        findNaturalPersonsService = new FindFamilyGroupNaturalPersonsService(familyGroupRepository);
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
                new SearchFamilyGroupQuery(null, null, null, null, null, null, null, PageRequest.of(0, 10)));

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

        List<NaturalPerson> result = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(saved.getId()));

        assertEquals(2, result.size());
        assertEquals("Daniela Ferreira", result.getFirst().getName());
        assertEquals("11144477735", result.getFirst().getCpf());
        assertEquals("11911112222", result.getFirst().getCellphone());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o grupo familiar não tiver pessoas vinculadas")
    void handle_ShouldReturnEmptyListWhenNoLinkedPersons() {
        FamilyGroup saved = createService.handle(buildCommand("Família Ferreira", "Joinville"));
        familyGroupRepository.flush();

        List<NaturalPerson> result = findNaturalPersonsService.handle(
                new FindFamilyGroupNaturalPersonsQuery(saved.getId()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve filtrar por termo de busca no nome")
    void handle_ShouldFilterBySearchTerm() {
        createService.handle(buildCommand("Família Silva", "São Paulo"));
        createService.handle(buildCommand("Família Souza", "Rio de Janeiro"));
        familyGroupRepository.flush();

        Page<FamilyGroupSummaryResponse> result = searchService.handle(
                new SearchFamilyGroupQuery("silva", null, null, null, null, null, null, PageRequest.of(0, 10)));

        assertEquals(1, result.getTotalElements());
        assertEquals("Família Silva", result.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Deve filtrar por faixa de renda familiar e número de moradores")
    void handle_ShouldFilterByIncomeRangeAndResidents() {
        createService.handle(buildCommand("Família Alfa", "São Paulo"));
        createService.handle(buildCommand("Família Beta", "Curitiba"));
        familyGroupRepository.flush();

        Page<FamilyGroupSummaryResponse> result = searchService.handle(
                new SearchFamilyGroupQuery(null, null, new BigDecimal("0.00"), new BigDecimal("1000.00"),
                        null, null, 4, PageRequest.of(0, 10)));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar os grupos quando o filtro de renda e moradores é satisfeito")
    void handle_ShouldReturnGroupsWhenFiltersMatch() {
        createService.handle(buildCommand("Família Alfa", "São Paulo"));
        createService.handle(buildCommand("Família Beta", "Curitiba"));
        familyGroupRepository.flush();

        Page<FamilyGroupSummaryResponse> result = searchService.handle(
                new SearchFamilyGroupQuery(null, null, new BigDecimal("1000.00"), new BigDecimal("9000.00"),
                        null, null, 4, PageRequest.of(0, 10)));

        assertEquals(2, result.getTotalElements());
    }
}
