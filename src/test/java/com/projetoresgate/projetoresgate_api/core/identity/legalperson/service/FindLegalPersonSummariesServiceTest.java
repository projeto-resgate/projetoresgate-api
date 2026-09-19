package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.CompanyStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.enums.RegistrationStatus;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindLegalPersonSummariesService - Test")
class FindLegalPersonSummariesServiceTest {

    @Mock
    private LegalPersonRepository repository;

    @InjectMocks
    private FindLegalPersonSummariesService service;

    private LegalPerson buildPerson(String cnpj, String displayName) {
        return LegalPerson.create(
                cnpj, "Razão Social LTDA", null, displayName, null,
                RegistrationStatus.ACTIVE, CompanyStatus.ACTIVE,
                Address.create("01310-100", "1000", null, "Bela Vista", "São Paulo", "SP"), null);
    }

    @Test
    @DisplayName("Deve retornar summaries para os IDs informados")
    void handle_ShouldReturnSummariesForIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        LegalPerson person1 = buildPerson("11222333000181", "Empresa Alfa");
        LegalPerson person2 = buildPerson("98765432000198", "Empresa Beta");

        when(repository.findAllById(Set.of(id1, id2))).thenReturn(List.of(person1, person2));

        List<LegalPersonSummaryResponse> result = service.handle(Set.of(id1, id2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Empresa Alfa", result.get(0).displayName());
        assertEquals("11222333000181", result.get(0).cnpj());
        assertEquals(person1.getId(), result.get(0).id());
        assertEquals("Empresa Beta", result.get(1).displayName());
        assertEquals("98765432000198", result.get(1).cnpj());
        assertEquals(person2.getId(), result.get(1).id());

        verify(repository).findAllById(Set.of(id1, id2));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o conjunto de IDs for vazio")
    void handle_ShouldReturnEmptyListForEmptyIds() {
        List<LegalPersonSummaryResponse> result = service.handle(Set.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, never()).findAllById(anySet());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o conjunto de IDs for nulo")
    void handle_ShouldReturnEmptyListForNullIds() {
        List<LegalPersonSummaryResponse> result = service.handle(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, never()).findAllById(anySet());
    }
}