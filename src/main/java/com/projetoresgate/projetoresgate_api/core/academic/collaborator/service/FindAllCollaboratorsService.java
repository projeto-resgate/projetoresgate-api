package com.projetoresgate.projetoresgate_api.core.academic.collaborator.service;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.api.dto.CollaboratorSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository.CollaboratorRepository;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.FindAllCollaboratorsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.collaborator.usecase.query.FindAllCollaboratorsQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FindAllCollaboratorsService implements FindAllCollaboratorsUseCase {

    private final CollaboratorRepository repository;

    public FindAllCollaboratorsService(CollaboratorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaboratorSummaryResponse> handle(FindAllCollaboratorsQuery query) {
        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        return repository.searchRows(searchTerm, false,
                        PageRequest.of(query.pageable().getPageNumber(), query.pageable().getPageSize()))
                .map(CollaboratorSummaryResponse::fromRow);
    }
}
