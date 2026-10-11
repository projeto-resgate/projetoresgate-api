package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ReactivateLearnerCommand;
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
@DisplayName("ReactivateLearnerService - Test")
class ReactivateLearnerServiceTest {

    private static final UUID NATURAL_PERSON_ID = UUID.randomUUID();

    @Mock
    private LearnerRepository repository;

    @Mock
    private FindLearnerByIdUseCase findLearnerByIdUseCase;

    @InjectMocks
    private ReactivateLearnerService service;

    private static Learner givenDroppedLearner() {
        Learner learner = Learner.create(NATURAL_PERSON_ID, null, null, false, null, 2026, null, null);
        learner.dropOut("Mudou de cidade", LocalDate.of(2026, 5, 10));
        return learner;
    }

    private static LearnerResponse response() {
        return new LearnerResponse(UUID.randomUUID(), NATURAL_PERSON_ID, null, null, false, null, 2026,
                null, null, LearnerStatus.ACTIVE, List.of(), null);
    }

    @Test
    @DisplayName("Deve reativar o educando mantendo a desistência no histórico")
    void handle_ShouldReactivateLearner() {
        Learner learner = givenDroppedLearner();

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(repository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(response());

        service.handle(new ReactivateLearnerCommand(learner.getId(), "Retornou no próximo ano letivo"));

        ArgumentCaptor<Learner> captor = ArgumentCaptor.forClass(Learner.class);
        verify(repository).save(captor.capture());

        Learner saved = captor.getValue();
        assertEquals(LearnerStatus.ACTIVE, saved.getStatus());
        assertEquals(2, saved.getStatusHistory().size());
        assertEquals(LearnerStatusEventType.DROPOUT, saved.getStatusHistory().get(0).getType());
        assertEquals(LearnerStatusEventType.REACTIVATION, saved.getStatusHistory().get(1).getType());
        assertEquals("Retornou no próximo ano letivo", saved.getStatusHistory().get(1).getReason());
    }

    @Test
    @DisplayName("Deve devolver a resposta lida pelo caso de uso de busca pelo id do educando salvo")
    void handle_ShouldReturnFindByIdResponse() {
        Learner learner = givenDroppedLearner();
        LearnerResponse expected = response();

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(repository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(findLearnerByIdUseCase.handle(any(FindLearnerByIdQuery.class))).thenReturn(expected);

        LearnerResponse result = service.handle(new ReactivateLearnerCommand(learner.getId(), null));

        assertSame(expected, result);

        ArgumentCaptor<FindLearnerByIdQuery> queryCaptor = ArgumentCaptor.forClass(FindLearnerByIdQuery.class);
        verify(findLearnerByIdUseCase).handle(queryCaptor.capture());
        assertEquals(learner.getId(), queryCaptor.getValue().id());
    }

    @Test
    @DisplayName("Não deve salvar quando o educando não estiver desistente")
    void handle_ShouldFailWhenNotDropped() {
        Learner learner = Learner.create(NATURAL_PERSON_ID, null, null, false, null, 2026, null, null);

        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        assertThrows(IllegalStateException.class,
                () -> service.handle(new ReactivateLearnerCommand(learner.getId(), null)));

        verify(repository, never()).save(any(Learner.class));
    }

    @Test
    @DisplayName("Deve propagar erro quando o educando não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class,
                () -> service.handle(new ReactivateLearnerCommand(id, null)));

        verify(repository, never()).save(any(Learner.class));
    }
}
