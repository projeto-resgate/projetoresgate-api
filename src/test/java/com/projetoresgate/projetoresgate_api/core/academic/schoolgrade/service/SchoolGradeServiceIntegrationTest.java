package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.service;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeNameResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.api.dto.SchoolGradeResponse;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository.SchoolGradeRepository;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.CreateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.SoftDeleteSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.command.UpdateSchoolGradeCommand;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindAllSchoolGradesQuery;
import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.usecase.query.FindSchoolGradeNamesQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import com.projetoresgate.projetoresgate_api.shared.testcontainers.PostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("SchoolGradeService - Integração")
class SchoolGradeServiceIntegrationTest extends PostgresIntegrationTest {

    private static final Pageable BY_ORDER = PageRequest.of(0, 50, Sort.by("gradeOrder").ascending());

    @Autowired
    private SchoolGradeRepository repository;

    @Autowired
    private EntityManager entityManager;

    private CreateSchoolGradeService createService;
    private UpdateSchoolGradeService updateService;
    private SoftDeleteSchoolGradeService softDeleteService;
    private FindAllSchoolGradesService findAllService;
    private FindSchoolGradeNamesService findNamesService;

    @BeforeEach
    void setUp() {
        createService = new CreateSchoolGradeService(repository);
        updateService = new UpdateSchoolGradeService(repository);
        softDeleteService = new SoftDeleteSchoolGradeService(repository);
        findAllService = new FindAllSchoolGradesService(repository);
        findNamesService = new FindSchoolGradeNamesService(repository);
    }

    private SchoolGrade givenGrade(String name, int order) {
        return repository.save(SchoolGrade.create(name, order));
    }

    @Test
    @DisplayName("Deve persistir a série com a ordem na coluna correta e reler do banco")
    void handle_ShouldPersistAndReload() {
        SchoolGrade saved = createService.handle(new CreateSchoolGradeCommand("Primeiro ano do ensino médio", 1));
        repository.flush();
        entityManager.clear();

        SchoolGrade found = repository.findByIdOrThrow(saved.getId());

        assertEquals("Primeiro ano do ensino médio", found.getName());
        assertEquals(1, found.getGradeOrder());
        assertNotNull(found.getDateCreated());
    }

    @Test
    @DisplayName("Deve permitir duas séries com a mesma ordem e nomes diferentes")
    void handle_ShouldAllowSameOrderForDifferentNames() {
        givenGrade("Primeiro ano", 1);
        givenGrade("Primeiro ano - técnico", 1);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery(null, 1, BY_ORDER));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve rejeitar nome duplicado entre séries ativas")
    void handle_ShouldRejectDuplicateName() {
        givenGrade("Primeiro ano", 1);
        repository.flush();

        assertThrows(InternalException.class,
                () -> createService.handle(new CreateSchoolGradeCommand("Primeiro ano", 2)));
    }

    @Test
    @DisplayName("Deve filtrar por nome com ILIKE, ignorando maiúsculas e minúsculas")
    void handle_ShouldFilterByNameIgnoringCase() {
        givenGrade("Primeiro ano do ensino médio", 1);
        givenGrade("Segundo ano do ensino médio", 2);
        givenGrade("Nono ano do ensino fundamental", 9);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery("MÉDIO", null, BY_ORDER));

        assertEquals(2, result.getTotalElements());
        assertEquals(List.of("Primeiro ano do ensino médio", "Segundo ano do ensino médio"),
                result.getContent().stream().map(SchoolGradeResponse::name).toList());
    }

    @Test
    @DisplayName("O filtro por nome não deve trazer a série que não bate")
    void handle_ShouldExcludeNonMatchingName() {
        givenGrade("Primeiro ano", 1);
        givenGrade("Segundo ano", 2);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery("Segundo", null, BY_ORDER));

        assertEquals(1, result.getTotalElements());
        assertEquals("Segundo ano", result.getContent().getFirst().name());
    }

    @Test
    @DisplayName("O filtro por nome não deve interpretar % como curinga")
    void handle_ShouldTreatWildcardAsLiteral() {
        givenGrade("Primeiro ano", 1);
        givenGrade("Segundo ano", 2);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery("%", null, BY_ORDER));

        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve filtrar por ordem exata e não trazer as outras ordens")
    void handle_ShouldFilterByExactGradeOrder() {
        givenGrade("Primeiro ano", 1);
        givenGrade("Segundo ano", 2);
        givenGrade("Décimo ano", 10);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery(null, 1, BY_ORDER));

        assertEquals(1, result.getTotalElements());
        assertEquals("Primeiro ano", result.getContent().getFirst().name());
    }

    @Test
    @DisplayName("A ordem 1 não deve casar com a ordem 10 na busca exata")
    void handle_ShouldNotPartiallyMatchGradeOrder() {
        givenGrade("Primeiro ano", 1);
        givenGrade("Décimo ano", 10);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery(null, 1, BY_ORDER));

        assertEquals(List.of("Primeiro ano"),
                result.getContent().stream().map(SchoolGradeResponse::name).toList());
    }

    @Test
    @DisplayName("Deve combinar os filtros de nome e ordem com AND")
    void handle_ShouldCombineFiltersWithAnd() {
        givenGrade("Primeiro ano", 1);
        givenGrade("Segundo ano", 1);
        givenGrade("Primeiro ano do fundamental", 1);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> bothFilters = findAllService.handle(
                new FindAllSchoolGradesQuery("Primeiro ano", 1, BY_ORDER));
        Page<SchoolGradeResponse> wrongOrder = findAllService.handle(
                new FindAllSchoolGradesQuery("Primeiro ano", 2, BY_ORDER));

        assertEquals(2, bothFilters.getTotalElements());
        assertEquals(0, wrongOrder.getTotalElements());
    }

    @Test
    @DisplayName("Sem filtro informado, deve listar todas as séries ordenadas pela ordem")
    void handle_ShouldListAllOrderedByGradeOrder() {
        givenGrade("Segundo ano", 2);
        givenGrade("Primeiro ano", 1);
        givenGrade("Nono ano", 9);
        repository.flush();
        entityManager.clear();

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery(null, null, BY_ORDER));

        assertEquals(3, result.getTotalElements());
        assertEquals(List.of("Primeiro ano", "Segundo ano", "Nono ano"),
                result.getContent().stream().map(SchoolGradeResponse::name).toList());
    }

    @Test
    @DisplayName("Deve paginar preservando o total")
    void handle_ShouldPaginatePreservingTotal() {
        for (int i = 1; i <= 3; i++) {
            givenGrade("Série " + i, i);
        }
        repository.flush();
        entityManager.clear();

        Pageable firstPage = PageRequest.of(0, 2, Sort.by("gradeOrder").ascending());
        Pageable secondPage = PageRequest.of(1, 2, Sort.by("gradeOrder").ascending());

        Page<SchoolGradeResponse> page1 = findAllService.handle(new FindAllSchoolGradesQuery(null, null, firstPage));
        Page<SchoolGradeResponse> page2 = findAllService.handle(new FindAllSchoolGradesQuery(null, null, secondPage));

        assertEquals(2, page1.getContent().size());
        assertEquals(3, page1.getTotalElements());
        assertEquals(2, page1.getTotalPages());
        assertEquals(1, page2.getContent().size());
        assertEquals(3, page2.getTotalElements());
        assertEquals("Série 3", page2.getContent().getFirst().name());
    }

    @Test
    @DisplayName("Deve atualizar os dados persistidos")
    void handle_ShouldUpdatePersistedData() {
        SchoolGrade saved = createService.handle(new CreateSchoolGradeCommand("Primeiro ano", 1));
        repository.flush();

        updateService.handle(new UpdateSchoolGradeCommand(saved.getId(), "Primeiro ano do médio", 2));
        repository.flush();
        entityManager.clear();

        SchoolGrade found = repository.findByIdOrThrow(saved.getId());

        assertEquals("Primeiro ano do médio", found.getName());
        assertEquals(2, found.getGradeOrder());
    }

    @Test
    @DisplayName("Deve excluir a série de forma lógica, escondendo-a das consultas")
    void handle_ShouldSoftDelete() {
        SchoolGrade saved = createService.handle(new CreateSchoolGradeCommand("Primeiro ano", 1));
        givenGrade("Segundo ano", 2);
        repository.flush();

        softDeleteService.handle(new SoftDeleteSchoolGradeCommand(saved.getId()));
        repository.flush();
        entityManager.clear();

        assertTrue(repository.findById(saved.getId()).isEmpty());
        assertThrows(ResourceNotFoundException.class, () -> repository.findByIdOrThrow(saved.getId()));

        Page<SchoolGradeResponse> result = findAllService.handle(
                new FindAllSchoolGradesQuery(null, null, BY_ORDER));
        assertEquals(List.of("Segundo ano"),
                result.getContent().stream().map(SchoolGradeResponse::name).toList());
    }

    @Test
    @DisplayName("A série excluída não deve aparecer na busca de nomes")
    void handle_ShouldHideSoftDeletedFromNames() {
        SchoolGrade saved = createService.handle(new CreateSchoolGradeCommand("Primeiro ano", 1));
        givenGrade("Segundo ano", 2);
        repository.flush();

        softDeleteService.handle(new SoftDeleteSchoolGradeCommand(saved.getId()));
        repository.flush();
        entityManager.clear();

        List<SchoolGradeNameResponse> names = findNamesService.handle(new FindSchoolGradeNamesQuery("", 10));

        assertEquals(List.of("Segundo ano"), names.stream().map(SchoolGradeNameResponse::name).toList());
    }

    @Test
    @DisplayName("Deve permitir recadastrar o mesmo nome depois da exclusão lógica")
    void handle_ShouldAllowReusingNameAfterSoftDelete() {
        SchoolGrade saved = createService.handle(new CreateSchoolGradeCommand("Primeiro ano", 1));
        repository.flush();

        softDeleteService.handle(new SoftDeleteSchoolGradeCommand(saved.getId()));
        repository.flush();
        entityManager.clear();

        SchoolGrade recreated = createService.handle(new CreateSchoolGradeCommand("Primeiro ano", 1));
        repository.flush();
        entityManager.clear();

        assertNotEquals(saved.getId(), recreated.getId());
        assertEquals(1, findAllService.handle(
                new FindAllSchoolGradesQuery(null, null, BY_ORDER)).getTotalElements());
    }

    @Test
    @DisplayName("A busca de nomes deve ordenar pela ordem da série e respeitar o limite")
    void handle_ShouldReturnNamesOrderedAndLimited() {
        givenGrade("Nono ano", 9);
        givenGrade("Primeiro ano", 1);
        givenGrade("Segundo ano", 2);
        repository.flush();
        entityManager.clear();

        List<SchoolGradeNameResponse> all = findNamesService.handle(new FindSchoolGradeNamesQuery("", 10));
        List<SchoolGradeNameResponse> limited = findNamesService.handle(new FindSchoolGradeNamesQuery("", 2));

        assertEquals(List.of("Primeiro ano", "Segundo ano", "Nono ano"),
                all.stream().map(SchoolGradeNameResponse::name).toList());
        assertEquals(List.of(1, 2), limited.stream().map(SchoolGradeNameResponse::gradeOrder).toList());
    }

    @Test
    @DisplayName("A busca de nomes deve filtrar por nome ignorando caixa")
    void handle_ShouldFilterNamesByName() {
        givenGrade("Primeiro ano do ensino médio", 1);
        givenGrade("Décimo ano do ensino fundamental", 10);
        repository.flush();
        entityManager.clear();

        List<SchoolGradeNameResponse> names = findNamesService.handle(
                new FindSchoolGradeNamesQuery("ensino médio", 10));

        assertEquals(1, names.size());
        assertEquals("Primeiro ano do ensino médio", names.getFirst().name());
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar uma série inexistente")
    void handle_ShouldFailUpdatingMissingGrade() {
        UUID id = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class,
                () -> updateService.handle(new UpdateSchoolGradeCommand(id, "Primeiro ano", 1)));
    }
}
