package com.projetoresgate.projetoresgate_api.core.academic.classgroup.service;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.api.dto.ClassGroupSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository.ClassGroupRepository;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.FindAllClassGroupsUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.usecase.query.FindAllClassGroupsQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class FindAllClassGroupsService implements FindAllClassGroupsUseCase {

    private final ClassGroupRepository repository;

    public FindAllClassGroupsService(ClassGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassGroupSummaryResponse> handle(FindAllClassGroupsQuery query) {
        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm() : null;

        return repository.findRows(searchTerm, query.programId(), query.institutionId(), query.active(),
                        PageRequest.of(query.pageable().getPageNumber(), query.pageable().getPageSize()))
                .map(ClassGroupSummaryResponse::fromRow);
    }
}
