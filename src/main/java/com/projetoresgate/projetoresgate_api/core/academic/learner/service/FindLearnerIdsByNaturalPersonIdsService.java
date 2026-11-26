package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.FindLearnerIdsByNaturalPersonIdsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class FindLearnerIdsByNaturalPersonIdsService implements FindLearnerIdsByNaturalPersonIdsUseCase {

    private final LearnerRepository repository;

    public FindLearnerIdsByNaturalPersonIdsService(LearnerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, UUID> handle(Collection<UUID> naturalPersonIds) {
        if (isNull(naturalPersonIds) || naturalPersonIds.isEmpty()) {
            return Map.of();
        }

        return repository.findAllByNaturalPersonIdIn(naturalPersonIds).stream()
                .collect(Collectors.toMap(Learner::getNaturalPersonId, Learner::getId));
    }
}
