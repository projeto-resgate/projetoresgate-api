package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository.NaturalPersonRoleProjection;
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
@DisplayName("FindNaturalPersonRolesService - Test")
class FindNaturalPersonRolesServiceTest {

    @Mock
    private NaturalPersonRoleRepository repository;

    @InjectMocks
    private FindNaturalPersonRolesService service;

    private NaturalPersonRoleProjection givenProjection(UUID naturalPersonId, NaturalPersonRoleType role) {
        return new RoleProjection(naturalPersonId, role);
    }

    private record RoleProjection(UUID naturalPersonId, NaturalPersonRoleType role)
            implements NaturalPersonRoleProjection {
        @Override
        public UUID getNaturalPersonId() {
            return naturalPersonId;
        }

        @Override
        public NaturalPersonRoleType getRole() {
            return role;
        }
    }

    @Test
    @DisplayName("Deve agrupar os papéis de cada pessoa física")
    void handle_ShouldGroupRolesByNaturalPerson() {
        UUID learner = UUID.randomUUID();
        UUID responsible = UUID.randomUUID();

        when(repository.findRolesByNaturalPersonIdsIn(any())).thenReturn(List.of(
                givenProjection(learner, NaturalPersonRoleType.LEARNER),
                givenProjection(responsible, NaturalPersonRoleType.RESPONSIBLE)));

        Map<UUID, List<NaturalPersonRoleType>> result = service.handle(List.of(learner, responsible));

        assertEquals(List.of(NaturalPersonRoleType.LEARNER), result.get(learner));
        assertEquals(List.of(NaturalPersonRoleType.RESPONSIBLE), result.get(responsible));
    }

    @Test
    @DisplayName("Deve devolver mais de um papel quando a pessoa acumula os dois")
    void handle_ShouldReturnBothRolesWhenAccumulated() {
        UUID naturalPersonId = UUID.randomUUID();

        when(repository.findRolesByNaturalPersonIdsIn(any())).thenReturn(List.of(
                givenProjection(naturalPersonId, NaturalPersonRoleType.RESPONSIBLE),
                givenProjection(naturalPersonId, NaturalPersonRoleType.LEARNER)));

        Map<UUID, List<NaturalPersonRoleType>> result = service.handle(List.of(naturalPersonId));

        assertEquals(List.of(NaturalPersonRoleType.LEARNER, NaturalPersonRoleType.RESPONSIBLE),
                result.get(naturalPersonId));
    }

    @Test
    @DisplayName("Deve devolver mapa vazio e não consultar o repositório quando não houver ids")
    void handle_ShouldReturnEmptyMapWithoutQueryingWhenNoIds() {
        Map<UUID, List<NaturalPersonRoleType>> result = service.handle(List.of());

        assertTrue(result.isEmpty());
        verify(repository, never()).findRolesByNaturalPersonIdsIn(any());
    }

    @Test
    @DisplayName("Deve devolver mapa vazio quando a consulta não encontrar nenhum papel")
    void handle_ShouldReturnEmptyMapWhenNoRolesFound() {
        when(repository.findRolesByNaturalPersonIdsIn(any())).thenReturn(List.of());

        Map<UUID, List<NaturalPersonRoleType>> result =
                service.handle(List.of(UUID.randomUUID()));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve consultar os papéis em lote com todos os ids recebidos")
    void handle_ShouldQueryRolesInBatch() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(repository.findRolesByNaturalPersonIdsIn(any())).thenReturn(List.of());

        service.handle(ids);

        verify(repository).findRolesByNaturalPersonIdsIn(ids);
    }
}