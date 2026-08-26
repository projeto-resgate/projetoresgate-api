package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.Representative;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.SearchLegalPersonQuery;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchLegalPersonService - Test")
class SearchLegalPersonServiceTest {

    @Mock
    private LegalPersonRepository repository;

    @Mock
    private Root<LegalPerson> root;

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
    private ArgumentCaptor<Specification<LegalPerson>> specCaptor;

    @InjectMocks
    private SearchLegalPersonService service;

    private Address buildAddress() {
        return Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP");
    }

    private LegalPerson buildPerson() {
        return LegalPerson.create(
                "12345678000195", "Razão Social LTDA", "Nome Fantasia", "Display Name", "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, buildAddress(), null);
    }

    @Test
    @DisplayName("Deve buscar pessoas jurídicas com paginação")
    void handle_ShouldSearchWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchLegalPersonQuery searchQuery = new SearchLegalPersonQuery(null, null, null, null, null, pageable);
        Page<LegalPerson> expectedPage = new PageImpl<>(List.of());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<LegalPerson> result = service.handle(searchQuery);

        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve construir a Specification corretamente quando houver termo de busca")
    void handle_ShouldBuildSpecificationWithSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchLegalPersonQuery searchQuery = new SearchLegalPersonQuery("Razão", null, null, null, null, pageable);

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

        Specification<LegalPerson> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);

        capturedSpec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).like(any(), contains("razão"));
    }

    @Test
    @DisplayName("Deve construir a Specification com filtros de CNPJ, Razão Social, Status de Registro e Status da Empresa")
    void handle_ShouldBuildSpecificationWithSpecificFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchLegalPersonQuery searchQuery = new SearchLegalPersonQuery(
                null, "12345678000195", "Razão Social", RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE, pageable);

        doReturn(path).when(root).get(anyString());

        lenient().doReturn(path).when(path).get(anyString());
        lenient().doReturn(String.class).when(path).getJavaType();
        lenient().doReturn(mockExpression).when(cb).lower(any());
        lenient().doReturn(mockPredicate).when(cb).like(any(), anyString());
        lenient().doReturn(mockPredicate).when(cb).and(any(Predicate[].class));

        service.handle(searchQuery);

        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        Specification<LegalPerson> capturedSpec = specCaptor.getValue();
        capturedSpec.toPredicate(root, query, cb);

        verify(cb).like(any(), eq("%12345678000195%"));
        verify(cb).like(any(), eq("%razão social%"));
    }

    @Test
    @DisplayName("Deve retornar pessoas jurídicas com todos os campos preenchidos")
    void handle_ShouldReturnPersonsWithAllFields() {
        Pageable pageable = PageRequest.of(0, 10);
        SearchLegalPersonQuery searchQuery = new SearchLegalPersonQuery(null, null, null, null, null, pageable);

        LocalDateTime dateCreated = LocalDateTime.of(2025, 3, 10, 8, 45, 0);

        LegalPerson person = buildPerson();
        person.setDateCreated(dateCreated);

        Page<LegalPerson> expectedPage = new PageImpl<>(List.of(person));
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<LegalPerson> result = service.handle(searchQuery);

        assertEquals(1, result.getContent().size());

        LegalPerson found = result.getContent().getFirst();
        assertEquals("12345678000195", found.getCnpj());
        assertEquals("Razão Social LTDA", found.getCorporateName());
        assertEquals("Nome Fantasia", found.getTradeName());
        assertEquals("Display Name", found.getDisplayName());
        assertEquals("6201-5/00", found.getMainCnaeCode());
        assertEquals(RegistrationStatus.ACTIVE, found.getRegistrationStatus());
        assertEquals(CompanyStatus.ACTIVE, found.getCompanyStatus());
        assertEquals("São Paulo", found.getAddress().getCity());
        assertEquals(dateCreated, found.getDateCreated());
    }
}
