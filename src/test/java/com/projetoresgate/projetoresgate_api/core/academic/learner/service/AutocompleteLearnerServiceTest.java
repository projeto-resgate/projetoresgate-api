package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.AutocompleteLearnerQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AutocompleteLearnerService - Test")
class AutocompleteLearnerServiceTest {

    @Mock
    private LearnerRepository repository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private AutocompleteLearnerService service;

    @Test
    @DisplayName("Deve retornar educandos com nome e CPF da pessoa física")
    void handle_ShouldReturnLearnersWithPersonData() {
        NaturalPerson person = NaturalPerson.create("Ana Souza", "ana@example.com", "aninha",
                "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(learner)));
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(person));

        List<LearnerAutocompleteResponse> result = service.handle(new AutocompleteLearnerQuery("ana", 10));

        assertEquals(1, result.size());
        assertEquals("Ana Souza", result.getFirst().name());
        assertEquals("52998224725", result.getFirst().cpf());
        assertTrue(result.getFirst().active());
    }

    @Test
    @DisplayName("Sem resultados deve retornar lista vazia")
    void handle_ShouldReturnEmptyWhenNoMatches() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<LearnerAutocompleteResponse> result = service.handle(new AutocompleteLearnerQuery("zzz", 10));

        assertTrue(result.isEmpty());
    }
}
