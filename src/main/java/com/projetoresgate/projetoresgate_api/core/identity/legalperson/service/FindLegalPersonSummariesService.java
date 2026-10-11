package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.api.dto.LegalPersonSummaryResponse;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonSummariesUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.isNull;

@Service
public class FindLegalPersonSummariesService implements FindLegalPersonSummariesUseCase {

    private final LegalPersonRepository repository;

    public FindLegalPersonSummariesService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LegalPersonSummaryResponse> handle(Set<UUID> ids) {
        if (isNull(ids) || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(ids).stream()
                .map(LegalPersonSummaryResponse::fromEntity)
                .toList();
    }
}