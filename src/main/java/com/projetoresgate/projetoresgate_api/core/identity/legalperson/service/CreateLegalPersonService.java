package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.CreateLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.CreateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class CreateLegalPersonService implements CreateLegalPersonUseCase {

    private final LegalPersonRepository repository;

    public CreateLegalPersonService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LegalPerson handle(CreateLegalPersonCommand command) {
        String cnpj = command.cnpj();

        if (nonNull(cnpj) && !cnpj.isBlank() && repository.existsByCnpj(cnpj)) {
            throw new InternalException("Já existe uma empresa cadastrada com este CNPJ.");
        }

        LegalPerson person = LegalPerson.create(
                command.cnpj(),
                command.corporateName(),
                command.tradeName(),
                command.displayName(),
                command.mainCnaeCode(),
                command.registrationStatus(),
                command.companyStatus(),
                command.address(),
                command.representative()
        );

        return repository.save(person);
    }
}
