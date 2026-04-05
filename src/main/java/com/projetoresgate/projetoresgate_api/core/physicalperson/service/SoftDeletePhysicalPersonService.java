package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.SoftDeletePhysicalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.SoftDeletePhysicalPersonCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeletePhysicalPersonService implements SoftDeletePhysicalPersonUseCase {

    private final PhysicalPersonRepository repository;

    public SoftDeletePhysicalPersonService(PhysicalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeletePhysicalPersonCommand command) {
        PhysicalPerson person = repository.findByIdOrThrow(command.id());
        repository.delete(person);
    }
}
