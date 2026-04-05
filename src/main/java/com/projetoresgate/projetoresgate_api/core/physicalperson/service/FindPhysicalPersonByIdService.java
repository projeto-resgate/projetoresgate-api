package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.FindPhysicalPersonByIdUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.query.FindPhysicalPersonByIdQuery;
import org.springframework.stereotype.Service;

@Service
public class FindPhysicalPersonByIdService implements FindPhysicalPersonByIdUseCase {

    private final PhysicalPersonRepository repository;

    public FindPhysicalPersonByIdService(PhysicalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public PhysicalPerson handle(FindPhysicalPersonByIdQuery query) {
        return repository.findByIdOrThrow(query.id());
    }
}
