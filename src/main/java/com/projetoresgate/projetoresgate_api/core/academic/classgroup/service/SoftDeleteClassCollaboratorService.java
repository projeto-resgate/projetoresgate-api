package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassCollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.SoftDeleteClassCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.command.SoftDeleteClassCollaboratorCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteClassCollaboratorService implements SoftDeleteClassCollaboratorUseCase {

    private final ClassCollaboratorRepository repository;

    public SoftDeleteClassCollaboratorService(ClassCollaboratorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteClassCollaboratorCommand command) {
        ClassCollaborator collaborator = repository.findByIdAndClassGroupIdOrThrow(command.id(), command.classGroupId());
        repository.delete(collaborator);
    }
}
