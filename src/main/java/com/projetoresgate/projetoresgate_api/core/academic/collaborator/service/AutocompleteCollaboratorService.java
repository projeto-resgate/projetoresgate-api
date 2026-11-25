package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.AutocompleteCollaboratorUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.AutocompleteCollaboratorQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AutocompleteCollaboratorService implements AutocompleteCollaboratorUseCase {

    private final CollaboratorRepository repository;

    public AutocompleteCollaboratorService(CollaboratorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollaboratorAutocompleteResponse> handle(AutocompleteCollaboratorQuery query) {
        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        return repository.searchRows(searchTerm, true,
                        PageRequest.of(0, query.limit()))
                .map(CollaboratorAutocompleteResponse::fromRow)
                .getContent();
    }
}
