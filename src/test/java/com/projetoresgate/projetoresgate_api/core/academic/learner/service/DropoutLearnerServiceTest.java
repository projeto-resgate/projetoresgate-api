package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.DropoutLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DropoutLearnerService - Test")
class DropoutLearnerServiceTest {

    private static final UUID NATURAL_PERSON_ID = UUID.randomUUID();

    @Mock
    private LearnerRepository repository;

    @Mock
    private FindLearnerByIdUseCase findLearnerByIdUseCase;

    @InjectMocks
    private DropoutLearnerService service;

    private static Learner givenLearner() {
        return Learner.create(NATURAL_PERSON_ID, null, null, false, null, 2026, null, null);
    }

    private static LearnerResponse response() {
        return new LearnerResponse(UUID.randomUUID(), NATURAL_PERSON_ID, null, null, false, null, 2026,
                null, null, LearnerStatus.DROPPED_OUT, List.of(), null);
    }

    @Test
    @DisplayName("Deve marcar o educando como desistente com motivo e data informados")
    void handle_ShouldDropOutLearner() {
        Learner learner = givenLearner();
        LocalDate eventDate = LocalDate.of(2026, 5, 10);

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(repository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(response());

        service.handle(new DropoutLearnerCommand(learner.getId(), "Mudou de cidade", eventDate));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(captor.capture());

        Learner saved = captor.getValue();
        assertEquals(LearnerStatus.DROPPED_OUT, saved.getStatus());
        assertEquals(1, saved.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.DROPOUT, saved.getStatusHistory().get(0).getType());
        assertEquals("Mudou de cidade", saved.getStatusHistory().get(0).getReason());
        assertEquals(eventDate, saved.getStatusHistory().get(0).getEventDate());
    }

    @Test
    @DisplayName("Deve devolver a resposta lida pelo caso de uso de busca pelo id do educando salvo")
    void handle_ShouldReturnFindByIdResponse() {
        Learner learner = givenLearner();
        LearnerResponse expected = response();

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(repository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(expected);

        LearnerResponse result = service.handle(
                new DropoutLearnerCommand(learner.getId(), "Mudou de cidade", null));

        assertSame(expected, result);

        ArgumentCaptor<FindLearnerByIdQuery> queryCaptor = ArgumentCaptor.forClass(FindLearnerByIdQuery.class);
        verify(findLearnerByIdUseCase).handle(queryCaptor.capture());
        assertEquals(learner.getId(), queryCaptor.getValue().id());
    }

    @Test
    @DisplayName("Não deve salvar quando o educando já estiver desistente")
    void handle_ShouldFailWhenAlreadyDropped() {
        Learner learner = givenLearner();
        learner.dropOut("Primeira desistência", LocalDate.of(2026, 5, 10));

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        assertThrows(IllegalStateException.class, () -> service.handle(
                new DropoutLearnerCommand(learner.getId(), "Segunda desistência", null)));

        verify(repository, never()).save(any(Learner.class));
    }

    @Test
    @DisplayName("Deve propagar erro quando o educando não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new DropoutLearnerCommand(id, "Mudou de cidade", null)));

        verify(repository, never()).save(any(Learner.class));
    }
}
