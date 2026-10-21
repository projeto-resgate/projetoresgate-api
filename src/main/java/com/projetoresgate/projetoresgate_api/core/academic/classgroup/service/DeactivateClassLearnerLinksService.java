package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.DeactivateClassLearnerLinksUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.DeactivateClassLearnerLinksCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateClassLearnerLinksService implements DeactivateClassLearnerLinksUseCase {

    private final ClassLearnerRepository repository;

    public DeactivateClassLearnerLinksService(ClassLearnerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public int handle(DeactivateClassLearnerLinksCommand command) {
        return repository.deactivateByLearnerId(command.learnerId());
    }
}
