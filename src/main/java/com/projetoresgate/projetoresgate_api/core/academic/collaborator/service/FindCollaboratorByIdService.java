package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.FindCollaboratorByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindCollaboratorByIdQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;

@Service
public class FindCollaboratorByIdService implements FindCollaboratorByIdUseCase {

    private final CollaboratorRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;

    public FindCollaboratorByIdService(CollaboratorRepository repository,
                                       NaturalPersonRepository naturalPersonRepository,
                                       LegalPersonRepository legalPersonRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CollaboratorResponse handle(FindCollaboratorByIdQuery query) {
        Collaborator collaborator = repository.findByIdOrThrow(query.id());

        NaturalPerson naturalPerson = naturalPersonRepository.findById(collaborator.getNaturalPersonId())
                .orElse(null);
        LegalPerson legalPerson = isNull(collaborator.getLegalPersonId()) ? null
                : legalPersonRepository.findById(collaborator.getLegalPersonId()).orElse(null);

        return CollaboratorResponse.fromEntity(collaborator, naturalPerson, legalPerson);
    }
}
