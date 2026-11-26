package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.LearnerProgramRow;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerParticipationStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindAllLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindAllLearnersService - Test")
class FindAllLearnersServiceTest {

    @Mock
    private LearnerRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @Mock
    private FindLegalPersonSummariesUseCase findLegalPersonSummariesUseCase;

    @Mock
    private ClassLearnerRepository classLearnerRepository;

    @InjectMocks
    private FindAllLearnersService service;

    @Test
    @DisplayName("Deve retornar a página resolvendo o nome da pessoa e da instituição")
    void handle_ShouldReturnPageResolvingNames() {
        UUID institutionId = UUID.randomUUID();
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), institutionId, null, false, null, 2026, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Learner> page = new PageImpl<>(List.of(learner));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(person));
        when(findLegalPersonSummariesUseCase.handle(any()))
                .thenReturn(List.of(new LegalPersonSummaryResponse(institutionId, "Escola X", "12345678000195")));

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertEquals(1, result.getTotalElements());
        LearnerSummaryResponse response = result.getContent().get(0);
        assertEquals(learner.getId(), response.id());
        assertEquals("Maria Silva", response.name());
        assertEquals("Escola X", response.institutionName());
        assertEquals(2026, response.academicYear());
        assertEquals(LearnerParticipationStatus.NO_CLASS, response.status());
        assertEquals(List.of(), response.programNames());
    }

    @Test
    @DisplayName("Deve devolver os programas das turmas de cada educando, sem misturar educandos")
    void handle_ShouldReturnProgramNamesPerLearner() {
        NaturalPerson maria = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        NaturalPerson joao = NaturalPerson.create(
                "João Lima", "joao@email.com", null, "93089835207", null, null, null, null, null);
        Learner mariaLearner = Learner.create(maria.getId(), null, null, false, null, 2026, null, null);
        Learner joaoLearner = Learner.create(joao.getId(), null, null, false, null, 2026, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(mariaLearner, joaoLearner)));
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(maria, joao));
        when(classLearnerRepository.findActiveProgramNamesByLearnerIds(any())).thenReturn(List.of(
                programRow(mariaLearner.getId(), "Música"),
                programRow(mariaLearner.getId(), "Robótica")));

        List<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable)).getContent();

        assertEquals(List.of("Música", "Robótica"), result.get(0).programNames());
        assertEquals(List.of(), result.get(1).programNames());
    }

    private static LearnerProgramRow programRow(UUID learnerId, String programName) {
        return new LearnerProgramRow() {
            @Override
            public UUID getLearnerId() {
                return learnerId;
            }

            @Override
            public String getProgramName() {
                return programName;
            }
        };
    }

    @Test
    @DisplayName("Deve marcar como DROPPED_OUT o educando desistente")
    void handle_ShouldMarkDroppedOutLearner() {
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        learner.dropOut("Mudou de cidade", LocalDate.now());
        Pageable pageable = PageRequest.of(0, 10);
        Page<Learner> page = new PageImpl<>(List.of(learner));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(person));

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertEquals(LearnerParticipationStatus.DROPPED_OUT, result.getContent().get(0).status());
    }

    @Test
    @DisplayName("Não deve consultar instituições quando o educando não tem instituição")
    void handle_ShouldSkipInstitutionsWhenNotSet() {
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Learner> page = new PageImpl<>(List.of(learner));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(person));

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertNull(result.getContent().get(0).institutionName());
        verify(findLegalPersonSummariesUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("Deve retornar página vazia sem consultar nomes")
    void handle_ShouldReturnEmptyPageWithoutLookups() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Learner> page = new PageImpl<>(List.of());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertTrue(result.getContent().isEmpty());
        verify(naturalPersonRepository, never()).findAllById(any());
        verify(findLegalPersonSummariesUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("Deve marcar como ACTIVE o educando que cursa turma em andamento")
    void handle_ShouldReturnActiveWhenInProgressClass() {
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(learner)));
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(person));
        when(classLearnerRepository.findLearnerIdsInProgress(any(), any())).thenReturn(List.of(learner.getId()));

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertEquals(LearnerParticipationStatus.ACTIVE, result.getContent().get(0).status());
    }

    @Test
    @DisplayName("Deve marcar como NO_CLASS o educando ativo sem turma em andamento")
    void handle_ShouldReturnNoClassWhenNoInProgressClass() {
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(learner)));
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(person));
        when(classLearnerRepository.findLearnerIdsInProgress(any(), any())).thenReturn(List.of());

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertEquals(LearnerParticipationStatus.NO_CLASS, result.getContent().get(0).status());
    }

    @Test
    @DisplayName("Deve marcar como DROPPED_OUT o desistente, mesmo com vínculo em turma em andamento")
    void handle_ShouldReturnDroppedOutEvenWithClassLink() {
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        learner.dropOut("Mudou de cidade", LocalDate.now());
        Pageable pageable = PageRequest.of(0, 10);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(learner)));
        when(naturalPersonRepository.findAllByIdIncludingDeleted(any())).thenReturn(List.of(person));
        when(classLearnerRepository.findLearnerIdsInProgress(any(), any())).thenReturn(List.of(learner.getId()));

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertEquals(LearnerParticipationStatus.DROPPED_OUT, result.getContent().get(0).status());
    }
}
