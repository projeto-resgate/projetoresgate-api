package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.CreateLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.CreateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.CnpjUtils;
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
        String cnpj = CnpjUtils.onlyDigits(command.cnpj());

        if (nonNull(cnpj) && !cnpj.isBlank() && repository.existsByCnpj(cnpj)) {
            throw new InternalException("Já existe uma empresa cadastrada com este CNPJ.");
        }

        LegalPerson person = LegalPerson.create(
                cnpj,
                command.corporateName(),
                command.tradeName(),
                command.displayName(),
                command.mainCnaeCode(),
                command.registrationStatus(),
                command.companyStatus(),
                toAddress(command.address()),
                command.representative()
        );

        return repository.save(person);
    }

    private Address toAddress(AddressCommand command) {
        if (command == null) {
            return null;
        }

        return Address.create(
                command.zipCode(),
                command.number(),
                command.complement(),
                command.neighborhood(),
                command.city(),
                command.state()
        );
    }
}
