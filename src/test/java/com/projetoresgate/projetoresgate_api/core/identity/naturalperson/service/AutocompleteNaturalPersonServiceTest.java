package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.api.dto.NaturalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.Gender;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.AutocompleteNaturalPersonQuery;
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

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AutocompleteNaturalPersonService - Test")
class AutocompleteNaturalPersonServiceTest {

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
    private AutocompleteNaturalPersonService service;

    private NaturalPerson buildPerson(String name, String cpf, String rg) {
        return NaturalPerson.create(
                name, "test@test.com", "apelido", cpf, rg,
                LocalDate.of(1990, 1, 1), Gender.FEMALE, "1133334444", "11999999999");
    }

    @Test
    @DisplayName("Deve retornar as summaries das pessoas encontradas pelo termo")
    void handle_ShouldReturnSummaries() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());
        AutocompleteNaturalPersonQuery query = new AutocompleteNaturalPersonQuery("jo", 5);

        NaturalPerson person1 = buildPerson("João Silva", "51086174968", "1234567");
        NaturalPerson person2 = buildPerson("José Souza", "11144477735", "7654321");
        Page<NaturalPerson> personPage = new PageImpl<>(List.of(person1, person2));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(personPage);

        List<NaturalPersonSummaryResponse> result = service.handle(query);

        assertEquals(2, result.size());
        assertEquals(person1.getId(), result.get(0).id());
        assertEquals("João Silva", result.get(0).name());
        assertEquals("1234567", result.get(0).rg());
        assertEquals("51086174968", result.get(0).cpf());
        assertEquals(person2.getId(), result.get(1).id());
        assertEquals("José Souza", result.get(1).name());
        assertEquals("7654321", result.get(1).rg());
        assertEquals("11144477735", result.get(1).cpf());

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve buscar o top-N mesmo sem searchTerm (em branco)")
    void handle_ShouldSearchAllWhenBlankSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        AutocompleteNaturalPersonQuery query = new AutocompleteNaturalPersonQuery("  ");

        NaturalPerson person = buildPerson("João Silva", "51086174968", "1234567");
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(person)));

        List<NaturalPersonSummaryResponse> result = service.handle(query);

        assertEquals(1, result.size());
        assertEquals("João Silva", result.get(0).name());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve buscar o top-N mesmo sem searchTerm (nulo)")
    void handle_ShouldSearchAllWhenNullSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        AutocompleteNaturalPersonQuery query = new AutocompleteNaturalPersonQuery(null);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        List<NaturalPersonSummaryResponse> result = service.handle(query);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve construir a Specification buscando por nome, nickname, dígitos do CPF e RG")
    void handle_ShouldBuildSpecification() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        AutocompleteNaturalPersonQuery searchQuery = new AutocompleteNaturalPersonQuery("Jo silva cpf 123", 10);

        doReturn(path).when(root).get(anyString());

        lenient().doReturn(String.class).when(path).getJavaType();
        lenient().doReturn(mockExpression).when(cb).lower(any());
        lenient().doReturn(mockExpression).when(cb).upper(any());
        lenient().doReturn(mockPredicate).when(cb).like(any(), anyString());
        lenient().doReturn(mockPredicate).when(cb).or(any(Predicate[].class));

        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.handle(searchQuery);

        verify(repository).findAll(specCaptor.capture(), eq(pageable));

        Specification<NaturalPerson> capturedSpec = specCaptor.getValue();
        capturedSpec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).like(any(), eq("%jo silva cpf 123%"));
        verify(cb, atLeastOnce()).like(any(), eq("%123%"));
        verify(cb, atLeastOnce()).like(any(), eq("%JOSILVACPF123%"));
    }
}