package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.ReactivateLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ReactivateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReactivateLearnerService implements ReactivateLearnerUseCase {

    private final LearnerRepository repository;
    private final FindLearnerByIdUseCase findLearnerByIdUseCase;

    public ReactivateLearnerService(LearnerRepository repository, FindLearnerByIdUseCase findLearnerByIdUseCase) {
        this.repository = repository;
        this.findLearnerByIdUseCase = findLearnerByIdUseCase;
    }

    @Override
    @Transactional
    public LearnerResponse handle(ReactivateLearnerCommand command) {
        Learner learner = repository.findByIdOrThrow(command.id());

        learner.reactivate(command.note(), null);

        Learner saved = repository.save(learner);

        return findLearnerByIdUseCase.handle(new FindLearnerByIdQuery(saved.getId()));
    }
}
