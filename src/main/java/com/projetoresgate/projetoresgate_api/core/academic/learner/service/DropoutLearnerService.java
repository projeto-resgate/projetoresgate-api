package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.DeactivateClassLearnerLinksUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.DeactivateClassLearnerLinksCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.DropoutLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.DropoutLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.FindLearnerByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.DropoutLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.FindLearnerByIdQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DropoutLearnerService implements DropoutLearnerUseCase {

    private final LearnerRepository repository;
    private final FindLearnerByIdUseCase findLearnerByIdUseCase;
    private final DeactivateClassLearnerLinksUseCase deactivateClassLearnerLinksUseCase;

    public DropoutLearnerService(LearnerRepository repository,
                                 FindLearnerByIdUseCase findLearnerByIdUseCase,
                                 DeactivateClassLearnerLinksUseCase deactivateClassLearnerLinksUseCase) {
        this.repository = repository;
        this.findLearnerByIdUseCase = findLearnerByIdUseCase;
        this.deactivateClassLearnerLinksUseCase = deactivateClassLearnerLinksUseCase;
    }

    @Override
    @Transactional
    public DropoutLearnerResponse handle(DropoutLearnerCommand command) {
        Learner learner = repository.findByIdOrThrow(command.id());

        learner.dropOut(command.reason(), command.eventDate());

        Learner saved = repository.save(learner);

        int deactivatedClassLinks = deactivateClassLearnerLinksUseCase.handle(
                new DeactivateClassLearnerLinksCommand(saved.getId()));

        LearnerResponse response = findLearnerByIdUseCase.handle(new FindLearnerByIdQuery(saved.getId()));

        return DropoutLearnerResponse.fromEntity(response, deactivatedClassLinks);
    }
}
