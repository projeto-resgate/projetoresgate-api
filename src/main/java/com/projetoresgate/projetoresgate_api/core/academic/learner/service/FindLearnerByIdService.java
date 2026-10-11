package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FindLearnerByIdService implements FindLearnerByIdUseCase {

    private final LearnerRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;

    public FindLearnerByIdService(LearnerRepository repository, NaturalPersonRepository naturalPersonRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LearnerResponse handle(FindLearnerByIdQuery query) {
        Learner learner = repository.findByIdOrThrow(query.id());

        return LearnerResponse.fromEntity(learner, resolveResponsibleNames(learner));
    }

    private Map<UUID, String> resolveResponsibleNames(Learner learner) {
        Set<UUID> naturalPersonIds = learner.getResponsibleList().stream()
                .map(Responsible::getNaturalPersonId)
                .collect(Collectors.toSet());

        if (naturalPersonIds.isEmpty()) {
            return Map.of();
        }

        return naturalPersonRepository.findAllById(naturalPersonIds).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, NaturalPerson::getName));
    }
}