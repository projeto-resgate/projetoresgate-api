package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.api.dto.FamilyGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.query.FindAllFamilyGroupsQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindAllFamilyGroupsService - Test")
class FindAllFamilyGroupsServiceTest {

    @Mock
    private FamilyGroupRepository repository;

    @Mock
    private Root<FamilyGroup> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Path<Object> numberOfResidentsPath;

    @Mock
    private Predicate mockPredicate;

    @Mock
    private Expression<String> mockExpression;

    @Captor
    private ArgumentCaptor<Specification<FamilyGroup>> specCaptor;

    @InjectMocks
    private FindAllFamilyGroupsService service;

    @Test
    @DisplayName("Deve listar grupos familiares com paginação")
    void handle_ShouldListWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        FindAllFamilyGroupsQuery searchQuery = new FindAllFamilyGroupsQuery(null, pageable);
        Page<FamilyGroup> foundPage = new PageImpl<>(List.of());

        when(repository.findAll(nullable(Specification.class), eq(pageable))).thenReturn(foundPage);

        Page<FamilyGroupSummaryResponse> result = service.handle(searchQuery);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(repository).findAll(nullable(Specification.class), eq(pageable));
        verify(repository, never()).countRegisteredPeopleByIds(any());
    }

    @Test
    @DisplayName("Deve construir a Specification com o filtro de nome")
    void handle_ShouldBuildSpecificationWithNameFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        FindAllFamilyGroupsQuery searchQuery = new FindAllFamilyGroupsQuery("Família Silva", pageable);

        doReturn(path).when(root).get("name");
        lenient().when(repository.findAll(nullable(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        lenient().doReturn(path).when(path).get(anyString());
        lenient().doReturn(String.class).when(path).getJavaType();
        lenient().doReturn(mockExpression).when(cb).lower(any());
        lenient().doReturn(mockPredicate).when(cb).like(any(), anyString(), anyChar());
        lenient().doReturn(mockPredicate).when(cb).and(any(Predicate[].class));

        service.handle(searchQuery);

        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        Specification<FamilyGroup> capturedSpec = specCaptor.getValue();
        capturedSpec.toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%família silva%"), eq('\\'));
    }

    @Test
    @DisplayName("Deve retornar o resumo dos grupos familiares com o total de pessoas cadastradas")
    void handle_ShouldReturnSummariesWithRegisteredPeopleCount() {
        Pageable pageable = PageRequest.of(0, 10);
        FindAllFamilyGroupsQuery searchQuery = new FindAllFamilyGroupsQuery(null, pageable);

        FamilyGroup silva = FamilyGroup.create(
                "FAM-1", "Família Silva", new BigDecimal("5000.00"), new BigDecimal("1250.00"),
                new BigDecimal("800.00"), new BigDecimal("400.00"), new BigDecimal("1500.00"), 4, null);
        FamilyGroup souza = FamilyGroup.create(
                "FAM-1", "Família Souza", new BigDecimal("3000.00"), new BigDecimal("1000.00"),
                new BigDecimal("300.00"), new BigDecimal("200.00"), new BigDecimal("900.00"), 3, null);

        List<FamilyGroupRepository.NaturalPersonCountProjection> counts =
                List.of(count(silva.getId(), 2L), count(souza.getId(), 5L));

        when(repository.findAll(nullable(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(silva, souza)));
        when(repository.countRegisteredPeopleByIds(List.of(silva.getId(), souza.getId()))).thenReturn(counts);

        Page<FamilyGroupSummaryResponse> result = service.handle(searchQuery);

        assertEquals(2, result.getContent().size());

        FamilyGroupSummaryResponse foundSilva = result.getContent().getFirst();
        assertEquals(silva.getId(), foundSilva.id());
        assertEquals(silva.getFriendlyId(), foundSilva.friendlyId());
        assertEquals("Família Silva", foundSilva.name());
        assertEquals(2L, foundSilva.registeredPeopleCount());

        FamilyGroupSummaryResponse foundSouza = result.getContent().get(1);
        assertEquals("Família Souza", foundSouza.name());
        assertEquals(5L, foundSouza.registeredPeopleCount());
    }

    @Test
    @DisplayName("Deve retornar zero pessoas cadastradas quando o grupo não tiver ninguém vinculado")
    void handle_ShouldReturnZeroWhenGroupHasNoLinkedPersons() {
        Pageable pageable = PageRequest.of(0, 10);
        FindAllFamilyGroupsQuery searchQuery = new FindAllFamilyGroupsQuery(null, pageable);

        FamilyGroup familyGroup = FamilyGroup.create(
                "FAM-1", "Família Silva", new BigDecimal("5000.00"), new BigDecimal("1250.00"),
                new BigDecimal("800.00"), new BigDecimal("400.00"), new BigDecimal("1500.00"), 4, null);

        List<FamilyGroupRepository.NaturalPersonCountProjection> counts =
                List.of(count(familyGroup.getId(), 0L));

        when(repository.findAll(nullable(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(familyGroup)));
        when(repository.countRegisteredPeopleByIds(List.of(familyGroup.getId()))).thenReturn(counts);

        Page<FamilyGroupSummaryResponse> result = service.handle(searchQuery);

        assertEquals(0L, result.getContent().getFirst().registeredPeopleCount());
    }

    private FamilyGroupRepository.NaturalPersonCountProjection count(UUID familyGroupId, Long count) {
        FamilyGroupRepository.NaturalPersonCountProjection projection =
                mock(FamilyGroupRepository.NaturalPersonCountProjection.class);
        lenient().when(projection.getFamilyGroupId()).thenReturn(familyGroupId);
        lenient().when(projection.getRegisteredPeopleCount()).thenReturn(count);
        return projection;
    }
}
