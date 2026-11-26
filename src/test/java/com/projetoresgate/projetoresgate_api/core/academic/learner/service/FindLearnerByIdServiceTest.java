package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.ResponsibleResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindLearnerByIdService - Test")
class FindLearnerByIdServiceTest {

    @Mock
    private LearnerRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private FindLearnerByIdService service;

    private static NaturalPerson givenPerson(String name) {
        return NaturalPerson.create(name, "pessoa@example.com", null, "52998224725",
                "123456789", null, null, null, null);
    }

    @Test
    @DisplayName("Deve devolver os dados do educando sem responsáveis e não consultar pessoas físicas")
    void handle_ShouldReturnLearnerWithoutResponsibles() {
        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);

        LearnerResponse response = service.handle(new FindLearnerByIdQuery(learner.getId()));

        assertEquals(learner.getId(), response.id());
        assertEquals(learner.getNaturalPersonId(), response.naturalPersonId());
        assertEquals(2026, response.academicYear());
        assertTrue(response.responsibleList().isEmpty());
        verifyNoInteractions(naturalPersonRepository);
    }

    @Test
    @DisplayName("Deve trazer o nome de cada responsável junto do vínculo")
    void handle_ShouldResolveResponsibleNames() {
        NaturalPerson mother = givenPerson("Maria Silva");
        NaturalPerson father = givenPerson("João Pereira");

        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        learner.syncResponsibleList(List.of(
                Responsible.create(learner, mother.getId(), ResponsibleType.FINANCIAL, Kinship.MOTHER, null),
                Responsible.create(learner, father.getId(), ResponsibleType.COMMUNICATION, Kinship.FATHER, null)));
        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(mother, father));

        LearnerResponse response = service.handle(new FindLearnerByIdQuery(learner.getId()));

        assertEquals(2, response.responsibleList().size());
        assertEquals(mother.getId(), response.responsibleList().get(0).naturalPersonId());
        assertEquals("Maria Silva", response.responsibleList().get(0).name());
        assertEquals(father.getId(), response.responsibleList().get(1).naturalPersonId());
        assertEquals("João Pereira", response.responsibleList().get(1).name());
    }

    @Test
    @DisplayName("Deve mapear tipo, parentesco e escolaridade de cada responsável")
    void handle_ShouldMapResponsibleFields() {
        NaturalPerson mother = givenPerson("Maria Silva");
        UUID schoolGradeId = UUID.randomUUID();

        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        learner.syncResponsibleList(List.of(Responsible.create(
                learner, mother.getId(), ResponsibleType.FINANCIAL, Kinship.MOTHER, schoolGradeId)));
        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(mother));

        LearnerResponse response = service.handle(new FindLearnerByIdQuery(learner.getId()));

        ResponsibleResponse responsible = response.responsibleList().get(0);
        assertEquals(ResponsibleType.FINANCIAL, responsible.type());
        assertEquals(Kinship.MOTHER, responsible.kinship());
        assertEquals(schoolGradeId, responsible.schoolGradeId());
    }

    @Test
    @DisplayName("Deve consultar as pessoas físicas dos responsáveis em uma única busca")
    void handle_ShouldResolveNamesInSingleQuery() {
        NaturalPerson mother = givenPerson("Maria Silva");
        NaturalPerson father = givenPerson("João Pereira");

        Learner learner = Learner.create(UUID.randomUUID(), null, null, false, null, 2026, null, null);
        learner.syncResponsibleList(List.of(
                Responsible.create(learner, mother.getId(), ResponsibleType.FINANCIAL, Kinship.MOTHER, null),
                Responsible.create(learner, father.getId(), ResponsibleType.COMMUNICATION, Kinship.FATHER, null)));
        when(repository.findByIdOrThrow(learner.getId())).thenReturn(learner);
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(mother, father));

        service.handle(new FindLearnerByIdQuery(learner.getId()));

        verify(naturalPersonRepository, times(1)).findAllById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o educando não existe")
    void handle_ShouldFailWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdOrThrow(id))
                .thenThrow(new ResourceNotFoundException("Educando não encontrado com ID: " + id));

        assertThrows(ResourceNotFoundException.class, () -> service.handle(new FindLearnerByIdQuery(id)));
    }
}