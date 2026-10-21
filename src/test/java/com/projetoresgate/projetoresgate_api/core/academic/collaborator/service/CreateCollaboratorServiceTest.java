package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.CreateCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCollaboratorService - Test")
class CreateCollaboratorServiceTest {

    @Mock
    private CollaboratorRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @InjectMocks
    private CreateCollaboratorService service;

    @Test
    @DisplayName("Deve criar o colaborador sem pessoa jurídica")
    void handle_ShouldCreateWithoutLegalPerson() {
        UUID naturalPersonId = UUID.randomUUID();

        when(naturalPersonRepository.findByIdOrThrow(naturalPersonId)).thenReturn(mock(NaturalPerson.class));
        when(repository.existsByNaturalPersonId(naturalPersonId)).thenReturn(false);
        when(repository.save(any(Collaborator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CollaboratorResponse response = service.handle(
                new CreateCollaboratorCommand(naturalPersonId, null, true));

        ArgumentCaptor<Collaborator> captor = ArgumentCaptor.forClass(Collaborator.class);
        verify(repository).save(captor.capture());

        Collaborator saved = captor.getValue();
        assertEquals(naturalPersonId, saved.getNaturalPersonId());
        assertNull(saved.getLegalPersonId());
        assertTrue(saved.isVoluntary());
        assertEquals(naturalPersonId, response.naturalPersonId());
        assertTrue(response.isVoluntary());
        verifyNoInteractions(legalPersonRepository);
    }

    @Test
    @DisplayName("Deve validar a pessoa jurídica quando informada")
    void handle_ShouldValidateLegalPersonWhenInformed() {
        UUID naturalPersonId = UUID.randomUUID();
        UUID legalPersonId = UUID.randomUUID();

        when(naturalPersonRepository.findByIdOrThrow(naturalPersonId)).thenReturn(mock(NaturalPerson.class));
        when(legalPersonRepository.findByIdOrThrow(legalPersonId)).thenReturn(mock(LegalPerson.class));
        when(repository.existsByNaturalPersonId(naturalPersonId)).thenReturn(false);
        when(repository.save(any(Collaborator.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handle(new CreateCollaboratorCommand(naturalPersonId, legalPersonId, false));

        ArgumentCaptor<Collaborator> captor = ArgumentCaptor.forClass(Collaborator.class);
        verify(repository).save(captor.capture());

        assertEquals(legalPersonId, captor.getValue().getLegalPersonId());
    }

    @Test
    @DisplayName("Não deve permitir dois colaboradores para a mesma pessoa física")
    void handle_ShouldRejectDuplicateNaturalPerson() {
        UUID naturalPersonId = UUID.randomUUID();

        when(naturalPersonRepository.findByIdOrThrow(naturalPersonId)).thenReturn(mock(NaturalPerson.class));
        when(repository.existsByNaturalPersonId(naturalPersonId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.handle(
                new CreateCollaboratorCommand(naturalPersonId, null, false)));

        verify(repository, never()).save(any());
    }
}
