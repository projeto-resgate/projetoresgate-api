package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.EligibleResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindEligibleResponsiblesQuery;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindEligibleResponsiblesService - Test")
class FindEligibleResponsiblesServiceTest {

    private static final Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by("name").ascending());

    @Mock
    private FamilyGroupRepository repository;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private ResponsibleRepository responsibleRepository;

    @InjectMocks
    private FindEligibleResponsiblesService service;

    private static NaturalPerson givenPerson(String name) {
        return NaturalPerson.create(name, name.toLowerCase().replace(' ', '.') + "@example.com", null,
                "52998224725", "123456789", null, null, null, "11911112222");
    }

    @Test
    @DisplayName("Deve listar os responsáveis elegíveis excluindo a pessoa do educando")
    void handle_ShouldListEligibleResponsibles() {
        UUID familyGroupId = UUID.randomUUID();
        UUID learnerPersonId = UUID.randomUUID();
        NaturalPerson mother = givenPerson("Maria Silva");

        when(repository.findEligibleResponsiblesByFamilyGroupId(familyGroupId, Set.of(learnerPersonId), PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(mother), PAGEABLE, 1));

        Page<EligibleResponsibleResponse> page = service.handle(
                new FindEligibleResponsiblesQuery(familyGroupId, learnerPersonId, PAGEABLE));

        assertEquals(1, page.getTotalElements());
        assertEquals(mother.getId(), page.getContent().get(0).naturalPersonId());
        assertEquals("Maria Silva", page.getContent().get(0).name());
        assertEquals("52998224725", page.getContent().get(0).cpf());
        assertEquals("11911112222", page.getContent().get(0).cellphone());
    }

    @Test
    @DisplayName("Deve devolver página vazia quando o grupo não tem outros membros")
    void handle_ShouldReturnEmptyPageWhenNoEligibleResponsible() {
        UUID familyGroupId = UUID.randomUUID();
        UUID learnerPersonId = UUID.randomUUID();

        when(repository.findEligibleResponsiblesByFamilyGroupId(familyGroupId, Set.of(learnerPersonId), PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(), PAGEABLE, 0));

        Page<EligibleResponsibleResponse> page = service.handle(
                new FindEligibleResponsiblesQuery(familyGroupId, learnerPersonId, PAGEABLE));

        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    @DisplayName("Deve exigir que o grupo familiar exista antes de listar")
    void handle_ShouldFailWhenFamilyGroupNotFound() {
        UUID familyGroupId = UUID.randomUUID();
        UUID learnerPersonId = UUID.randomUUID();

        when(repository.findByIdOrThrow(familyGroupId))
                .thenThrow(new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + familyGroupId));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(
                new FindEligibleResponsiblesQuery(familyGroupId, learnerPersonId, PAGEABLE)));

        verify(repository, never()).findEligibleResponsiblesByFamilyGroupId(any(), any(), any());
    }

    @Test
    @DisplayName("Educando já cadastrado: deve excluir também quem já é responsável dele")
    void handle_ShouldExcludeCurrentResponsibles() {
        UUID familyGroupId = UUID.randomUUID();
        UUID learnerPersonId = UUID.randomUUID();
        UUID motherId = UUID.randomUUID();
        Learner learner = Learner.create(learnerPersonId, null, null, false, null, 2026, null, null);
        NaturalPerson uncle = givenPerson("Paulo Silva");

        when(learnerRepository.findByNaturalPersonId(learnerPersonId)).thenReturn(Optional.of(learner));
        when(responsibleRepository.findNaturalPersonIdsByLearnerId(learner.getId())).thenReturn(List.of(motherId));
        when(repository.findEligibleResponsiblesByFamilyGroupId(familyGroupId, Set.of(learnerPersonId, motherId), PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(uncle), PAGEABLE, 1));

        Page<EligibleResponsibleResponse> page = service.handle(
                new FindEligibleResponsiblesQuery(familyGroupId, learnerPersonId, PAGEABLE));

        assertEquals(List.of(uncle.getId()), page.getContent().stream().map(EligibleResponsibleResponse::naturalPersonId).toList());
    }
}
