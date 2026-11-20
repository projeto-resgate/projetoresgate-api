package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.ActivateClassLearnerLinkUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.ActivateClassLearnerLinkCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatus;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.ReactivateLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.ReactivateLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ReactivateLearnerService implements ReactivateLearnerUseCase {

    private final LearnerRepository repository;
    private final FindLearnerByIdUseCase findLearnerByIdUseCase;

    private final ActivateClassLearnerLinkUseCase activateClassLearnerLinkUseCase;

    public ReactivateLearnerService(LearnerRepository repository,
                                    FindLearnerByIdUseCase findLearnerByIdUseCase,
                                    ActivateClassLearnerLinkUseCase activateClassLearnerLinkUseCase) {
        this.repository = repository;
        this.findLearnerByIdUseCase = findLearnerByIdUseCase;
        this.activateClassLearnerLinkUseCase = activateClassLearnerLinkUseCase;
    }

    @Override
    @Transactional
    public LearnerResponse handle(ReactivateLearnerCommand command) {
        Learner learner = repository.findByIdOrThrow(command.id());

        boolean wasDroppedOut = learner.getStatus() == LearnerStatus.DROPPED_OUT;

        if (!wasDroppedOut && isNull(command.classGroupId())) {
            throw new IllegalStateException("O educando não está desistente.");
        }

        if (wasDroppedOut) {
            learner.reactivate(command.note(), null);
        }

        Learner saved = repository.save(learner);

        if (nonNull(command.classGroupId())) {
            activateClassLearnerLinkUseCase.handle(
                    new ActivateClassLearnerLinkCommand(command.classGroupId(), saved.getId()));
        }

        return findLearnerByIdUseCase.handle(new FindLearnerByIdQuery(saved.getId()));
    }
}
