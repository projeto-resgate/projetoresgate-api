package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.CreateClassCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.CreateClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.EducatorCategoryItem;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateClassCollaboratorService implements CreateClassCollaboratorUseCase {

    private final ClassGroupRepository classGroupRepository;
    private final ClassCollaboratorRepository repository;
    private final CollaboratorRepository collaboratorRepository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final EducatorCategoryItemRepository educatorCategoryItemRepository;

    public CreateClassCollaboratorService(ClassGroupRepository classGroupRepository,
                                          ClassCollaboratorRepository repository,
                                          CollaboratorRepository collaboratorRepository,
                                          NaturalPersonRepository naturalPersonRepository,
                                          EducatorCategoryItemRepository educatorCategoryItemRepository) {
        this.classGroupRepository = classGroupRepository;
        this.repository = repository;
        this.collaboratorRepository = collaboratorRepository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.educatorCategoryItemRepository = educatorCategoryItemRepository;
    }

    @Override
    @Transactional
    public ClassCollaboratorResponse handle(CreateClassCollaboratorCommand command) {
        ClassGroup classGroup = classGroupRepository.findByIdOrThrow(command.classGroupId());

        Collaborator collaborator = collaboratorRepository.findByIdOrThrow(command.collaboratorId());
        EducatorCategoryItem category = educatorCategoryItemRepository.findByIdAndProgramIdOrThrow(
                command.educatorCategoryItemId(), classGroup.getProgramId());

        if (repository.existsByClassGroupIdAndCollaboratorId(classGroup.getId(), command.collaboratorId())) {
            throw new IllegalStateException("Este educador já está vinculado à turma.");
        }

        ClassCollaborator classCollaborator = ClassCollaborator.create(
                classGroup, command.collaboratorId(), command.educatorCategoryItemId());

        ClassCollaborator saved = repository.save(classCollaborator);

        NaturalPerson person = naturalPersonRepository.findById(collaborator.getNaturalPersonId()).orElse(null);

        return ClassCollaboratorResponse.fromEntity(
                saved,
                person == null ? null : person.getName(),
                category.getName());
    }
}
