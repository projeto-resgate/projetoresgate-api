package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.FindLegalPersonByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.query.FindLegalPersonByIdQuery;
import org.springframework.stereotype.Service;

@Service
public class FindLegalPersonByIdService implements FindLegalPersonByIdUseCase {

    private final LegalPersonRepository repository;

    public FindLegalPersonByIdService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public LegalPerson handle(FindLegalPersonByIdQuery query) {
        return repository.findByIdOrThrow(query.id());
    }
}
