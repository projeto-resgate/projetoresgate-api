package com.projetoresgate.projetoresgate_api.core.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service.SearchNaturalPersonService;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.SearchNaturalPersonQuery;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchNaturalPersonService - Test")
class SearchNaturalPersonServiceTest {

    @Mock
    private NaturalPersonRepository repository;

    @Mock
    private Root<NaturalPerson> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> path;

    @Mock
    private Predicate mockPredicate;

    @Mock
    private Expression<String> mockExpression;

    @Captor
    private ArgumentCaptor<Specification<NaturalPerson>> specCaptor;

    @InjectMocks
    private SearchNaturalPersonService service;

    @Test
    @DisplayName("Deve buscar pessoas físicas com paginação")
    @SuppressWarnings("unchecked")
    void handle_ShouldSearchWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNaturalPersonQuery searchQuery = new SearchNaturalPersonQuery(null, null, null, null, null, pageable);
        Page<NaturalPerson> expectedPage = new PageImpl<>(List.of());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<NaturalPerson> result = service.handle(searchQuery);

        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve construir a Specification corretamente quando houver termo de busca")
    void handle_ShouldBuildSpecificationWithSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchNaturalPersonQuery searchQuery = new SearchNaturalPersonQuery("Ana 123", null, null, null, null, pageable);

        doReturn(path).when(root).get(anyString());

        lenient().doReturn(path).when(path).get(anyString());
        lenient().doReturn(String.class).when(path).getJavaType();

        lenient().doReturn(mockExpression).when(cb).lower(any());
        lenient().doReturn(mockExpression).when(cb).upper(any());
        lenient().doReturn(mockPredicate).when(cb).like(any(), anyString());
        lenient().doReturn(mockPredicate).when(cb).or(any(Predicate[].class));
        lenient().doReturn(mockPredicate).when(cb).and(any(Predicate[].class));

        service.handle(searchQuery);

        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        Specification<NaturalPerson> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);

        capturedSpec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).like(any(), contains("ana 123"));
        verify(cb, atLeastOnce()).like(any(), contains("123"));
    }

    @Test
    @DisplayName("Deve construir a Specification com filtros específicos de CPF, RG, Celular e Gênero")
    void handle_ShouldBuildSpecificationWithSpecificFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        String cpfValue = "22948025001";
        String rgValue = "7654321";
        String cellphone = "999999999";
        SearchNaturalPersonQuery searchQuery = new SearchNaturalPersonQuery(
                null, rgValue, cpfValue, cellphone, Gender.MALE, pageable);

        doReturn(path).when(root).get(anyString());

        lenient().doReturn(path).when(path).get(anyString());
        lenient().doReturn(String.class).when(path).getJavaType();
        lenient().doReturn(mockExpression).when(cb).lower(any());
        lenient().doReturn(mockPredicate).when(cb).like(any(), anyString());
        lenient().doReturn(mockPredicate).when(cb).and(any(Predicate[].class));

        service.handle(searchQuery);

        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        Specification<NaturalPerson> capturedSpec = specCaptor.getValue();
        capturedSpec.toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%" + cpfValue + "%"));
        verify(cb).like(any(), eq("%" + rgValue + "%"));
        verify(cb).like(any(), eq("%" + cellphone + "%"));
        verify(cb).like(any(), eq("%male%"));
    }
}
