package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerSummaryResponse;
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
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(person));
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
        assertTrue(response.active());
    }

    @Test
    @DisplayName("Deve marcar como inativo o educando desistente")
    void handle_ShouldMarkDroppedOutLearnerAsInactive() {
        NaturalPerson person = NaturalPerson.create(
                "Maria Silva", "maria@email.com", null, "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        learner.dropOut("Mudou de cidade", LocalDate.now());
        Pageable pageable = PageRequest.of(0, 10);
        Page<Learner> page = new PageImpl<>(List.of(learner));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(person));

        Page<LearnerSummaryResponse> result = service.handle(
                new FindAllLearnersQuery(null, null, null, null, null, pageable));

        assertFalse(result.getContent().get(0).active());
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
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(person));

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
}
