package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassLearnersQuery;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindAllClassLearnersService - Test")
class FindAllClassLearnersServiceTest {

    @Mock
    private ClassLearnerRepository repository;

    @Mock
    private ClassGroupRepository classGroupRepository;

    @Mock
    private LearnerRepository learnerRepository;

    @Mock
    private NaturalPersonRepository naturalPersonRepository;

    @InjectMocks
    private FindAllClassLearnersService service;

    @Test
    @DisplayName("Deve montar a resposta com o nome e o CPF da pessoa física do educando")
    void handle_ShouldResolveLearnerData() {
        ClassGroup classGroup = ClassGroup.create(UUID.randomUUID(), UUID.randomUUID(), 20,
                LocalDateTime.of(2026, 2, 1, 8, 0), 6, null, "Programa A", List.of());
        NaturalPerson person = NaturalPerson.create("Ana Souza", "ana@example.com", null,
                "52998224725", null, null, null, null, null);
        Learner learner = Learner.create(person.getId(), null, null, false, null, 2026, null, null);
        ClassLearner link = ClassLearner.create(classGroup, learner.getId());

        Pageable pageable = PageRequest.of(0, 10);
        when(classGroupRepository.findByIdOrThrow(classGroup.getId())).thenReturn(classGroup);
        when(repository.findAllByClassGroupId(eq(classGroup.getId()), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(link), pageable, 1));
        when(learnerRepository.findAllById(any())).thenReturn(List.of(learner));
        when(naturalPersonRepository.findAllById(any())).thenReturn(List.of(person));

        Page<ClassLearnerResponse> result = service.handle(
                new FindAllClassLearnersQuery(classGroup.getId(), null, null, pageable));

        assertEquals(1, result.getTotalElements());
        ClassLearnerResponse response = result.getContent().getFirst();
        assertEquals("Ana Souza", response.learnerName());
        assertEquals("52998224725", response.learnerCpf());
        assertEquals(classGroup.getId(), response.classGroupId());
    }
}
