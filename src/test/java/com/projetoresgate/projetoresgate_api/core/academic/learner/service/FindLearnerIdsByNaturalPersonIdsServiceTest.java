package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindLearnerIdsByNaturalPersonIdsService - Test")
class FindLearnerIdsByNaturalPersonIdsServiceTest {

    @Mock
    private LearnerRepository repository;

    @InjectMocks
    private FindLearnerIdsByNaturalPersonIdsService service;

    @Test
    @DisplayName("Deve mapear cada pessoa física educanda para o id do educando")
    void handle_ShouldMapNaturalPersonToLearnerId() {
        UUID danielaId = UUID.randomUUID();
        UUID brenoId = UUID.randomUUID();
        Learner learner = Learner.create(danielaId, null, null, false, null, 2026, null, null);
        when(repository.findAllByNaturalPersonIdIn(List.of(danielaId, brenoId))).thenReturn(List.of(learner));

        Map<UUID, UUID> result = service.handle(List.of(danielaId, brenoId));

        assertEquals(Map.of(danielaId, learner.getId()), result);
    }

    @Test
    @DisplayName("Não deve consultar o banco quando não houver pessoas")
    void handle_ShouldReturnEmptyWithoutQuerying_WhenNoIds() {
        assertTrue(service.handle(List.of()).isEmpty());
        assertTrue(service.handle(null).isEmpty());
        verify(repository, never()).findAllByNaturalPersonIdIn(any());
    }
}
