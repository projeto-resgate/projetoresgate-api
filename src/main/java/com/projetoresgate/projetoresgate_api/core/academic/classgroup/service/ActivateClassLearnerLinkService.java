package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.ActivateClassLearnerLinkUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.ActivateClassLearnerLinkCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateClassLearnerLinkService implements ActivateClassLearnerLinkUseCase {

    private final ClassGroupRepository classGroupRepository;
    private final ClassLearnerRepository repository;

    public ActivateClassLearnerLinkService(ClassGroupRepository classGroupRepository,
                                           ClassLearnerRepository repository) {
        this.classGroupRepository = classGroupRepository;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(ActivateClassLearnerLinkCommand command) {
        ClassGroup classGroup = classGroupRepository.findByIdOrThrow(command.classGroupId());

        repository.findByClassGroupIdAndLearnerId(classGroup.getId(), command.learnerId())
                .ifPresentOrElse(
                        ClassLearner::activate,
                        () -> repository.save(ClassLearner.create(classGroup, command.learnerId())));
    }
}
