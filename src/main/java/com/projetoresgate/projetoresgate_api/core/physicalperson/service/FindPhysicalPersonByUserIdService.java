package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.FindPhysicalPersonByUserIdUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByUserIdQuery;
import org.springframework.stereotype.Service;

@Service
public class FindPhysicalPersonByUserIdService implements FindPhysicalPersonByUserIdUseCase {

    private final PhysicalPersonRepository repository;

    public FindPhysicalPersonByUserIdService(PhysicalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public PhysicalPerson handle(FindPhysicalPersonByUserIdQuery query) {
        return repository.findByUserIdOrThrow(query.userId());
    }
}
