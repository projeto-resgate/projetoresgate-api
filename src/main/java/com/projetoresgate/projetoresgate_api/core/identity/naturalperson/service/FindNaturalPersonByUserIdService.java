package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.FindNaturalPersonByUserIdUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.query.FindNaturalPersonByUserIdQuery;
import org.springframework.stereotype.Service;

@Service
public class FindNaturalPersonByUserIdService implements FindNaturalPersonByUserIdUseCase {

    private final NaturalPersonRepository repository;

    public FindNaturalPersonByUserIdService(NaturalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public NaturalPerson handle(FindNaturalPersonByUserIdQuery query) {
        return repository.findByUserIdOrThrow(query.userId());
    }
}
