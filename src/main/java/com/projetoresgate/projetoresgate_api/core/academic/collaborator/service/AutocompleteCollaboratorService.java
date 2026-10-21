package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.AutocompleteCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.AutocompleteCollaboratorQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AutocompleteCollaboratorService implements AutocompleteCollaboratorUseCase {

    private final CollaboratorRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;

    public AutocompleteCollaboratorService(CollaboratorRepository repository,
                                           NaturalPersonRepository naturalPersonRepository,
                                           LegalPersonRepository legalPersonRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorAutocompleteResponse> handle(AutocompleteCollaboratorQuery query) {
        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        Pageable pageable = PageRequest.of(0, query.limit());
        List<Collaborator> collaborators = repository.search(searchTerm, pageable).getContent();

        Set<UUID> naturalPersonIds = collaborators.stream()
                .map(Collaborator::getNaturalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<UUID> legalPersonIds = collaborators.stream()
                .map(Collaborator::getLegalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, NaturalPerson> naturalPersons = naturalPersonIds.isEmpty() ? Map.of()
                : naturalPersonRepository.findAllById(naturalPersonIds).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, person -> person));
        Map<UUID, LegalPerson> legalPersons = legalPersonIds.isEmpty() ? Map.of()
                : legalPersonRepository.findAllById(legalPersonIds).stream()
                .collect(Collectors.toMap(LegalPerson::getId, legalPerson -> legalPerson));

        return collaborators.stream()
                .map(collaborator -> CollaboratorAutocompleteResponse.fromEntity(
                        collaborator,
                        naturalPersons.get(collaborator.getNaturalPersonId()),
                        legalPersonOf(legalPersons, collaborator)))
                .toList();
    }

    private static LegalPerson legalPersonOf(Map<UUID, LegalPerson> legalPersons, Collaborator collaborator) {
        UUID legalPersonId = collaborator.getLegalPersonId();
        return legalPersonId == null ? null : legalPersons.get(legalPersonId);
    }
}
