package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.SoftDeleteCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.SoftDeleteCollaboratorCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteCollaboratorService implements SoftDeleteCollaboratorUseCase {

    private final CollaboratorRepository repository;

    public SoftDeleteCollaboratorService(CollaboratorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteCollaboratorCommand command) {
        Collaborator collaborator = repository.findByIdOrThrow(command.id());
        repository.delete(collaborator);
    }
}
