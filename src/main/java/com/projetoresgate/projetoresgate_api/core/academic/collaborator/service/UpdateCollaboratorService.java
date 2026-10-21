package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.UpdateCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.command.UpdateCollaboratorCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class UpdateCollaboratorService implements UpdateCollaboratorUseCase {

    private final CollaboratorRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;

    public UpdateCollaboratorService(CollaboratorRepository repository,
                                     NaturalPersonRepository naturalPersonRepository,
                                     LegalPersonRepository legalPersonRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
    }

    @Override
    @Transactional
    public CollaboratorResponse handle(UpdateCollaboratorCommand command) {
        Collaborator collaborator = repository.findByIdOrThrow(command.id());

        NaturalPerson naturalPerson = naturalPersonRepository.findByIdOrThrow(command.naturalPersonId());

        LegalPerson legalPerson = nonNull(command.legalPersonId())
                ? legalPersonRepository.findByIdOrThrow(command.legalPersonId())
                : null;

        if (!command.naturalPersonId().equals(collaborator.getNaturalPersonId())
                && repository.existsByNaturalPersonIdAndIdNot(command.naturalPersonId(), collaborator.getId())) {
            throw new IllegalStateException("Esta pessoa física já possui um cadastro de colaborador.");
        }

        collaborator.update()
                .naturalPersonId(command.naturalPersonId())
                .legalPersonId(command.legalPersonId())
                .isVoluntary(command.isVoluntary())
                .apply();

        Collaborator saved = repository.save(collaborator);

        return CollaboratorResponse.fromEntity(saved, naturalPerson, legalPerson);
    }
}
