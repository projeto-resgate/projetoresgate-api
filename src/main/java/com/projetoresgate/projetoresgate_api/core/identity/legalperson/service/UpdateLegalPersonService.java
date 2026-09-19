package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.UpdateLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.UpdateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.infrastructure.utils.CnpjUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class UpdateLegalPersonService implements UpdateLegalPersonUseCase {

    private final LegalPersonRepository repository;

    public UpdateLegalPersonService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public LegalPerson handle(UpdateLegalPersonCommand command) {
        LegalPerson person = repository.findByIdOrThrow(command.id());

        String cnpj = CnpjUtils.onlyDigits(command.cnpj());
        if (nonNull(cnpj) && !cnpj.isBlank() && repository.existsByCnpjAndIdNot(cnpj, person.getId())) {
            throw new InternalException("Já existe uma empresa cadastrada com este CNPJ.");
        }

        updateAddress(person, command.address());

        return repository.save(
                person.update()
                        .cnpj(cnpj)
                        .corporateName(command.corporateName())
                        .tradeName(command.tradeName())
                        .displayName(command.displayName())
                        .mainCnaeCode(command.mainCnaeCode())
                        .registrationStatus(command.registrationStatus())
                        .companyStatus(command.companyStatus())
                        .representative(command.representative())
                        .apply()
        );
    }

    private void updateAddress(LegalPerson person, AddressCommand command) {
        if (command == null) {
            return;
        }

        Address current = nonNull(person.getAddress())
                ? person.getAddress()
                : Address.create(command.zipCode(), command.number(), command.complement(),
                command.neighborhood(), command.city(), command.state());
        current.update()
                .zipCode(command.zipCode())
                .number(command.number())
                .complement(command.complement())
                .neighborhood(command.neighborhood())
                .city(command.city())
                .state(command.state())
                .apply();

        if (!nonNull(person.getAddress())) {
            person.update().address(current).apply();
        }
    }
}
