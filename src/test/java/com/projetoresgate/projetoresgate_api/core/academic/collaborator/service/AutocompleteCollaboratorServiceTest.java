package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.AutocompleteCollaboratorQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AutocompleteCollaboratorService - Test")
class AutocompleteCollaboratorServiceTest {

    @Mock
    private CollaboratorRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private AutocompleteCollaboratorService service;

    @Test
    @DisplayName("Deve respeitar o limite e montar as sugestões")
    void handle_ShouldReturnSuggestionsRespectingLimit() {
        UUID naturalPersonId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, naturalPersonId, null);
        NaturalPerson naturalPerson = mock(NaturalPerson.class);
        when(naturalPerson.getId()).thenReturn(naturalPersonId);
        when(naturalPerson.getName()).thenReturn("Ana Souza");

        when(repository.search(eq("ana"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(collaborator)));
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(naturalPerson));

        List<CollaboratorAutocompleteResponse> result =
                service.handle(new AutocompleteCollaboratorQuery("ana", 5));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).search(eq("ana"), pageableCaptor.capture());

        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(5, pageableCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals("Ana Souza", result.get(0).naturalPersonName());
        verifyNoInteractions(legalPersonRepository);
    }
}
