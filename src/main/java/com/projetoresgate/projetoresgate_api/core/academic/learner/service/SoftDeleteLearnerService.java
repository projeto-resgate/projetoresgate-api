package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.ResponsibleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.SoftDeleteLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.command.SoftDeleteLearnerCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteLearnerService implements SoftDeleteLearnerUseCase {

    private final LearnerRepository repository;
    private final ResponsibleRepository responsibleRepository;
    private final LearnerRoleSynchronizer roleSynchronizer;

    public SoftDeleteLearnerService(LearnerRepository repository,
                                    ResponsibleRepository responsibleRepository,
                                    LearnerRoleSynchronizer roleSynchronizer) {
        this.repository = repository;
        this.responsibleRepository = responsibleRepository;
        this.roleSynchronizer = roleSynchronizer;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteLearnerCommand command) {
        Learner learner = repository.findByIdOrThrow(command.id());

        responsibleRepository.softDeleteByLearnerId(learner.getId());

        roleSynchronizer.revokeEverything(learner);

        repository.delete(learner);
    }
}