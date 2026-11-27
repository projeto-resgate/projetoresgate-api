package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.ActivateClassLearnerLinkUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.ActivateClassLearnerLinkCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateClassLearnerLinkService implements ActivateClassLearnerLinkUseCase {

    private final ClassGroupRepository classGroupRepository;
    private final ClassLearnerRepository repository;
    private final LearnerRepository learnerRepository;
    private final NaturalPersonRepository naturalPersonRepository;

    public ActivateClassLearnerLinkService(ClassGroupRepository classGroupRepository,
                                           ClassLearnerRepository repository,
                                           LearnerRepository learnerRepository,
                                           NaturalPersonRepository naturalPersonRepository) {
        this.classGroupRepository = classGroupRepository;
        this.repository = repository;
        this.learnerRepository = learnerRepository;
        this.naturalPersonRepository = naturalPersonRepository;
    }

    @Override
    @Transactional
    public void handle(ActivateClassLearnerLinkCommand command) {
        ClassGroup classGroup = classGroupRepository.findByIdOrThrow(command.classGroupId());
        Learner learner = learnerRepository.findByIdOrThrow(command.learnerId());
        learner.ensureCanJoinClassGroup();
        naturalPersonRepository.findByIdOrThrow(learner.getNaturalPersonId());

        repository.findByClassGroupIdAndLearnerId(classGroup.getId(), command.learnerId())
                .ifPresentOrElse(
                        ClassLearner::activate,
                        () -> repository.save(ClassLearner.create(classGroup, command.learnerId())));
    }
}
