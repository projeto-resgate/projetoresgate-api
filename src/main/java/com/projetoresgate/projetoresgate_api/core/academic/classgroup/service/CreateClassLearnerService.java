package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassLearnerResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassLearnerCommand;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateClassLearnerService implements CreateClassLearnerUseCase {

    private final ClassGroupRepository classGroupRepository;
    private final ClassLearnerRepository repository;
    private final LearnerRepository learnerRepository;
    private final NaturalPersonRepository naturalPersonRepository;

    public CreateClassLearnerService(ClassGroupRepository classGroupRepository,
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
    public ClassLearnerResponse handle(CreateClassLearnerCommand command) {
        ClassGroup classGroup = classGroupRepository.findByIdOrThrow(command.classGroupId());
        Learner learner = learnerRepository.findByIdOrThrow(command.learnerId());

        if (repository.existsByClassGroupIdAndLearnerId(classGroup.getId(), command.learnerId())) {
            throw new IllegalStateException("Este educando já está vinculado à turma.");
        }

        ClassLearner classLearner = ClassLearner.create(classGroup, command.learnerId());
        ClassLearner saved = repository.save(classLearner);

        NaturalPerson person = naturalPersonRepository.findById(learner.getNaturalPersonId()).orElse(null);

        return ClassLearnerResponse.fromEntity(
                saved,
                person == null ? null : person.getName(),
                person == null ? null : person.getCpf(),
                learner.getStatus());
    }
}
