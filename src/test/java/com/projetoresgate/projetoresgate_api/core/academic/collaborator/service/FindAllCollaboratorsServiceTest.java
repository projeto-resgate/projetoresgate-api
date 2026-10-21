package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindAllCollaboratorsService - Test")
class FindAllCollaboratorsServiceTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Mock
    private CollaboratorRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private FindAllCollaboratorsService service;

    @Test
    @DisplayName("Deve montar a página com os nomes das pessoas físicas e jurídicas")
    void handle_ShouldReturnPageWithNames() {
        UUID firstNaturalPersonId = UUID.randomUUID();
        UUID secondNaturalPersonId = UUID.randomUUID();
        UUID legalPersonId = UUID.randomUUID();

        Collaborator first = Collaborator.create(true, firstNaturalPersonId, null);
        Collaborator second = Collaborator.create(false, secondNaturalPersonId, legalPersonId);

        NaturalPerson firstNaturalPerson = mock(NaturalPerson.class);
        NaturalPerson secondNaturalPerson = mock(NaturalPerson.class);
        LegalPerson legalPerson = mock(LegalPerson.class);

        when(firstNaturalPerson.getId()).thenReturn(firstNaturalPersonId);
        when(secondNaturalPerson.getId()).thenReturn(secondNaturalPersonId);
        when(legalPerson.getId()).thenReturn(legalPersonId);
        when(firstNaturalPerson.getName()).thenReturn("Ana Souza");
        when(secondNaturalPerson.getName()).thenReturn("Bruno Lima");
        when(legalPerson.getDisplayName()).thenReturn("Empresa X LTDA");

        when(repository.search(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), PAGEABLE, 2));
        when(naturalPersonRepository.findAllById(any()))
                .thenReturn(List.of(firstNaturalPerson, secondNaturalPerson));
        when(legalPersonRepository.findAllById(any())).thenReturn(List.of(legalPerson));

        Page<CollaboratorSummaryResponse> page = service.handle(new FindAllCollaboratorsQuery(null, PAGEABLE));

        assertEquals(2, page.getTotalElements());
        assertEquals("Ana Souza", page.getContent().get(0).naturalPersonName());
        assertNull(page.getContent().get(0).legalPersonName());
        assertEquals("Bruno Lima", page.getContent().get(1).naturalPersonName());
        assertEquals("Empresa X LTDA", page.getContent().get(1).legalPersonName());
    }

    @Test
    @DisplayName("Deve converter termo em branco para busca nula")
    void handle_ShouldNormalizeBlankSearchTerm() {
        when(repository.search(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        service.handle(new FindAllCollaboratorsQuery("   ", PAGEABLE));

        verify(repository).search(isNull(), any(Pageable.class));
    }
}
