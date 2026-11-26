package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassScheduleRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassGroupCommand;
import com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard.Dependency.of;

@Service
public class SoftDeleteClassGroupService implements SoftDeleteClassGroupUseCase {

    private final ClassGroupRepository repository;
    private final ClassScheduleRepository scheduleRepository;

    public SoftDeleteClassGroupService(ClassGroupRepository repository,
                                       ClassScheduleRepository scheduleRepository) {
        this.repository = repository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteClassGroupCommand command) {
        ClassGroup classGroup = repository.findByIdOrThrow(command.id());

        ClassGroupRepository.ClassGroupDependencies dependencies = repository.countDependencies(classGroup.getId());
        DeletionGuard.ensureNoDependencies("a turma",
                of(dependencies.getLearners(), "educando", "educandos"),
                of(dependencies.getCollaborators(), "educador", "educadores"));

        scheduleRepository.softDeleteByClassGroupId(classGroup.getId());

        classGroup.getSchoolGradeList().clear();
        repository.delete(classGroup);
    }
}
