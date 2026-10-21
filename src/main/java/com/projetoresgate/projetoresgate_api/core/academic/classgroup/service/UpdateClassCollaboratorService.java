package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassCollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.UpdateClassCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.UpdateClassCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.program.repository.EducatorCategoryItemRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class UpdateClassCollaboratorService implements UpdateClassCollaboratorUseCase {

    private final ClassCollaboratorRepository repository;
    private final CollaboratorRepository collaboratorRepository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final EducatorCategoryItemRepository educatorCategoryItemRepository;

    public UpdateClassCollaboratorService(ClassCollaboratorRepository repository,
                                          CollaboratorRepository collaboratorRepository,
                                          NaturalPersonRepository naturalPersonRepository,
                                          EducatorCategoryItemRepository educatorCategoryItemRepository) {
        this.repository = repository;
        this.collaboratorRepository = collaboratorRepository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.educatorCategoryItemRepository = educatorCategoryItemRepository;
    }

    @Override
    @Transactional
    public ClassCollaboratorResponse handle(UpdateClassCollaboratorCommand command) {
        ClassCollaborator classCollaborator = repository.findByIdAndClassGroupIdOrThrow(
                command.id(), command.classGroupId());

        if (nonNull(command.educatorCategoryItemId())) {
            educatorCategoryItemRepository.findByIdAndProgramIdOrThrow(
                    command.educatorCategoryItemId(), classCollaborator.getClassGroup().getProgramId());
        }

        classCollaborator.update()
                .educatorCategoryItemId(command.educatorCategoryItemId())
                .active(command.active())
                .apply();

        ClassCollaborator saved = repository.save(classCollaborator);

        Collaborator collaborator = collaboratorRepository.findById(saved.getCollaboratorId()).orElse(null);
        NaturalPerson person = collaborator == null ? null
                : naturalPersonRepository.findById(collaborator.getNaturalPersonId()).orElse(null);
        String categoryName = educatorCategoryItemRepository.findById(saved.getEducatorCategoryItemId())
                .map(item -> item.getName())
                .orElse(null);

        return ClassCollaboratorResponse.fromEntity(
                saved,
                person == null ? null : person.getName(),
                categoryName);
    }
}
