package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.AutocompleteLegalPersonQuery;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AutocompleteLegalPersonService - Test")
class AutocompleteLegalPersonServiceTest {

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
    private AutocompleteLegalPersonService service;

    private LegalPerson buildPerson(String displayName, String cnpj) {
        return LegalPerson.create(
                cnpj, "Razão Social LTDA", "Nome Fantasia", displayName, "6201-5/00",
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP"), null);
    }

    @Test
    @DisplayName("Deve retornar as summaries das pessoas encontradas pelo termo")
    void handle_ShouldReturnSummaries() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("displayName").ascending());
        AutocompleteLegalPersonQuery query = new AutocompleteLegalPersonQuery("acme", 5);

        LegalPerson person1 = buildPerson("Acme LTDA", "11222333000181");
        LegalPerson person2 = buildPerson("Beta SA", "98765432000198");
        Page<LegalPerson> personPage = new PageImpl<>(List.of(person1, person2));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(personPage);

        List<LegalPersonSummaryResponse> result = service.handle(query);

        assertEquals(2, result.size());
        assertEquals(person1.getId(), result.get(0).id());
        assertEquals("Acme LTDA", result.get(0).displayName());
        assertEquals("11222333000181", result.get(0).cnpj());
        assertEquals(person2.getId(), result.get(1).id());
        assertEquals("Beta SA", result.get(1).displayName());
        assertEquals("98765432000198", result.get(1).cnpj());

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve buscar o top-N mesmo sem searchTerm (em branco)")
    void handle_ShouldSearchAllWhenBlankSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("displayName").ascending());
        AutocompleteLegalPersonQuery query = new AutocompleteLegalPersonQuery("   ");

        LegalPerson person = buildPerson("Acme LTDA", "11222333000181");
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(person)));

        List<LegalPersonSummaryResponse> result = service.handle(query);

        assertEquals(1, result.size());
        assertEquals("Acme LTDA", result.get(0).displayName());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve buscar o top-N mesmo sem searchTerm (nulo)")
    void handle_ShouldSearchAllWhenNullSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("displayName").ascending());
        AutocompleteLegalPersonQuery query = new AutocompleteLegalPersonQuery(null);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        List<LegalPersonSummaryResponse> result = service.handle(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve construir a Specification buscando por razão social, nome fantasia, display name e dígitos do CNPJ")
    void handle_ShouldBuildSpecification() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("displayName").ascending());
        AutocompleteLegalPersonQuery searchQuery = new AutocompleteLegalPersonQuery("Acme 123", 10);

        doReturn(path).when(root).get(anyString());

        lenient().doReturn(String.class).when(path).getJavaType();
        lenient().doReturn(mockExpression).when(cb).lower(any());
        lenient().doReturn(mockPredicate).when(cb).like(any(), anyString());
        lenient().doReturn(mockPredicate).when(cb).or(any(Predicate[].class));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.handle(searchQuery);

        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        Specification<LegalPerson> capturedSpec = specCaptor.getValue();
        capturedSpec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).like(any(), eq("%acme 123%"));
        verify(cb, atLeastOnce()).like(any(), eq("%123%"));
    }
}