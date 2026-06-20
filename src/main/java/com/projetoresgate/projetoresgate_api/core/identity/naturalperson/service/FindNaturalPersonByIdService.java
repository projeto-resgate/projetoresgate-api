package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.FindNaturalPersonByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByIdQuery;
import org.springframework.stereotype.Service;

@Service
public class FindNaturalPersonByIdService implements FindNaturalPersonByIdUseCase {

    private final NaturalPersonRepository repository;

    public FindNaturalPersonByIdService(NaturalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public NaturalPerson handle(FindNaturalPersonByIdQuery query) {
        return repository.findByIdOrThrow(query.id());
    }
}
