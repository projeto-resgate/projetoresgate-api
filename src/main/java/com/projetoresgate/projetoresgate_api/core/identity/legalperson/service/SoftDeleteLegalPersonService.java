package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.SoftDeleteLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.SoftDeleteLegalPersonCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteLegalPersonService implements SoftDeleteLegalPersonUseCase {

    private final LegalPersonRepository repository;

    public SoftDeleteLegalPersonService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteLegalPersonCommand command) {
        LegalPerson person = repository.findByIdOrThrow(command.id());
        repository.delete(person);
    }
}