package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.UpdateCollaboratorCommand;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCollaboratorService - Test")
class UpdateCollaboratorServiceTest {

    @Mock
    private CollaboratorRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private UpdateCollaboratorService service;

    @Test
    @DisplayName("Deve atualizar os dados mantendo a mesma pessoa física")
    void handle_ShouldUpdateFields() {
        UUID naturalPersonId = UUID.randomUUID();
        UUID legalPersonId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, naturalPersonId, null);

        when(repository.findByIdOrThrow(collaborator.getId())).thenReturn(collaborator);
        when(naturalPersonRepository.findByIdOrThrow(naturalPersonId)).thenReturn(mock(NaturalPerson.class));
        when(legalPersonRepository.findByIdOrThrow(legalPersonId)).thenReturn(mock(LegalPerson.class));
        when(repository.save(collaborator)).thenReturn(collaborator);

        service.handle(new UpdateCollaboratorCommand(collaborator.getId(), naturalPersonId, legalPersonId, true));

        assertEquals(legalPersonId, collaborator.getLegalPersonId());
        assertTrue(collaborator.isVoluntary());
        verify(repository, never()).existsByNaturalPersonIdAndIdNot(any(), any());
    }

    @Test
    @DisplayName("Deve limpar a pessoa jurídica quando nulo")
    void handle_ShouldClearLegalPerson() {
        UUID naturalPersonId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, naturalPersonId, UUID.randomUUID());

        when(repository.findByIdOrThrow(collaborator.getId())).thenReturn(collaborator);
        when(naturalPersonRepository.findByIdOrThrow(naturalPersonId)).thenReturn(mock(NaturalPerson.class));
        when(repository.save(collaborator)).thenReturn(collaborator);

        service.handle(new UpdateCollaboratorCommand(collaborator.getId(), naturalPersonId, null, false));

        assertNull(collaborator.getLegalPersonId());
        verifyNoInteractions(legalPersonRepository);
    }

    @Test
    @DisplayName("Deve recusar trocar para uma pessoa física que já possui colaborador")
    void handle_ShouldRejectWhenNaturalPersonAlreadyTaken() {
        UUID currentNaturalPersonId = UUID.randomUUID();
        UUID newNaturalPersonId = UUID.randomUUID();
        Collaborator collaborator = Collaborator.create(false, currentNaturalPersonId, null);

        when(repository.findByIdOrThrow(collaborator.getId())).thenReturn(collaborator);
        when(naturalPersonRepository.findByIdOrThrow(newNaturalPersonId)).thenReturn(mock(NaturalPerson.class));
        when(repository.existsByNaturalPersonIdAndIdNot(eq(newNaturalPersonId), any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.handle(
                new UpdateCollaboratorCommand(collaborator.getId(), newNaturalPersonId, null, false)));

        verify(repository, never()).save(any());
    }
}
