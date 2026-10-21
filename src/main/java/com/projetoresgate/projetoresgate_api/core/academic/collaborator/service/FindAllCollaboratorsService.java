package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.FindAllCollaboratorsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class FindAllCollaboratorsService implements FindAllCollaboratorsUseCase {

    private final CollaboratorRepository repository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;

    public FindAllCollaboratorsService(CollaboratorRepository repository,
                                       NaturalPersonRepository naturalPersonRepository,
                                       LegalPersonRepository legalPersonRepository) {
        this.repository = repository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaboratorSummaryResponse> handle(FindAllCollaboratorsQuery query) {
        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        Page<Collaborator> page = repository.search(searchTerm, query.pageable());

        Map<UUID, NaturalPerson> naturalPersons = resolveNaturalPersons(page.getContent());
        Map<UUID, LegalPerson> legalPersons = resolveLegalPersons(page.getContent());

        List<CollaboratorSummaryResponse> content = page.getContent().stream()
                .map(collaborator -> CollaboratorSummaryResponse.fromEntity(
                        collaborator,
                        naturalPersons.get(collaborator.getNaturalPersonId()),
                        legalPersonOf(legalPersons, collaborator)))
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    private Map<UUID, NaturalPerson> resolveNaturalPersons(List<Collaborator> collaborators) {
        Set<UUID> ids = collaborators.stream()
                .map(Collaborator::getNaturalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return naturalPersonRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NaturalPerson::getId, person -> person));
    }

    private Map<UUID, LegalPerson> resolveLegalPersons(List<Collaborator> collaborators) {
        Set<UUID> ids = collaborators.stream()
                .map(Collaborator::getLegalPersonId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        return legalPersonRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(LegalPerson::getId, legalPerson -> legalPerson));
    }

    private static LegalPerson legalPersonOf(Map<UUID, LegalPerson> legalPersons, Collaborator collaborator) {
        UUID legalPersonId = collaborator.getLegalPersonId();
        return legalPersonId == null ? null : legalPersons.get(legalPersonId);
    }
}
