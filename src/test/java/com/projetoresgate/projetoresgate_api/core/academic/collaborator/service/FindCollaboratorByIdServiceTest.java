package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindCollaboratorByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindCollaboratorByIdService - Test")
class FindCollaboratorByIdServiceTest {

    @Mock
    private CollaboratorRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private FindCollaboratorByIdService service;

    @Test
    @DisplayName("Deve montar a resposta do colaborador pelo id")
    void handle_ShouldReturnResponse() {
        UUID naturalPersonId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(true, naturalPersonId, null);

        NaturalPerson person = mock(NaturalPerson.class);
        when(person.getName()).thenReturn("Ana Souza");
        when(person.getCpf()).thenReturn("52998224725");

        when(repository.findByIdOrThrow(collaborator.getId())).thenReturn(collaborator);
        when(naturalPersonRepository.findById(naturalPersonId)).thenReturn(Optional.of(person));

        CollaboratorResponse response = service.handle(new FindCollaboratorByIdQuery(collaborator.getId()));

        assertEquals(collaborator.getId(), response.id());
        assertEquals(naturalPersonId, response.naturalPersonId());
        assertEquals("Ana Souza", response.naturalPersonName());
        assertEquals("52998224725", response.naturalPersonCpf());
        assertNull(response.legalPersonName());
        verifyNoInteractions(legalPersonRepository);
    }
}
