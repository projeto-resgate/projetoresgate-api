package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerStatusHistoryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerStatusHistoryRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerStatusHistoryUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerStatusHistoryQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindLearnerStatusHistoryService implements FindLearnerStatusHistoryUseCase {

    private final LearnerRepository learnerRepository;
    private final LearnerStatusHistoryRepository historyRepository;

    public FindLearnerStatusHistoryService(LearnerRepository learnerRepository,
                                           LearnerStatusHistoryRepository historyRepository) {
        this.learnerRepository = learnerRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerStatusHistoryResponse> handle(FindLearnerStatusHistoryQuery query) {
        learnerRepository.findByIdOrThrow(query.learnerId());

        return historyRepository.findAllByLearnerIdOrderByEventDateDescDateCreatedDesc(query.learnerId()).stream()
                .map(LearnerStatusHistoryResponse::fromEntity)
                .toList();
    }
}
