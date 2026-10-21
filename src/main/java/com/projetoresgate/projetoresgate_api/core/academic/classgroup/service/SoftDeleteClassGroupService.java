package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassLearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassGroupCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteClassGroupService implements SoftDeleteClassGroupUseCase {

    private final ClassGroupRepository repository;
    private final ClassScheduleRepository scheduleRepository;
    private final ClassCollaboratorRepository collaboratorRepository;
    private final ClassLearnerRepository learnerRepository;

    public SoftDeleteClassGroupService(ClassGroupRepository repository,
                                       ClassScheduleRepository scheduleRepository,
                                       ClassCollaboratorRepository collaboratorRepository,
                                       ClassLearnerRepository learnerRepository) {
        this.repository = repository;
        this.scheduleRepository = scheduleRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.learnerRepository = learnerRepository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteClassGroupCommand command) {
        ClassGroup classGroup = repository.findByIdOrThrow(command.id());

        scheduleRepository.softDeleteByClassGroupId(classGroup.getId());
        collaboratorRepository.softDeleteByClassGroupId(classGroup.getId());
        learnerRepository.softDeleteByClassGroupId(classGroup.getId());

        classGroup.getSchoolGradeList().clear();
        repository.delete(classGroup);
    }
}
