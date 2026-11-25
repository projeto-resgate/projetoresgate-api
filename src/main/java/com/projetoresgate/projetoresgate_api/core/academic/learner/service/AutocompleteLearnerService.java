package com.projetoresgate.projetoresgate_api.core.academic.learner.service;

import com.projetoresgate.projetoresgate_api.core.academic.learner.api.dto.LearnerAutocompleteResponse;
import com.projetoresgate.projetoresgate_api.core.academic.learner.repository.LearnerRepository;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.AutocompleteLearnerUseCase;
import com.projetoresgate.projetoresgate_api.core.academic.learner.usecase.query.AutocompleteLearnerQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AutocompleteLearnerService implements AutocompleteLearnerUseCase {

    private final LearnerRepository repository;

    public AutocompleteLearnerService(LearnerRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerAutocompleteResponse> handle(AutocompleteLearnerQuery query) {
        String searchTerm = StringUtils.hasText(query.searchTerm()) ? query.searchTerm().trim() : null;
        return repository.autocomplete(searchTerm, query.limit()).stream()
                .map(LearnerAutocompleteResponse::fromRow)
                .toList();
    }
}
