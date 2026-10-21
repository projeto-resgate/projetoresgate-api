package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassLearnerCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteClassLearnerService implements SoftDeleteClassLearnerUseCase {

    private final ClassLearnerRepository repository;

    public SoftDeleteClassLearnerService(ClassLearnerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteClassLearnerCommand command) {
        ClassLearner classLearner = repository.findByIdAndClassGroupIdOrThrow(command.id(), command.classGroupId());
        repository.delete(classLearner);
    }
}
