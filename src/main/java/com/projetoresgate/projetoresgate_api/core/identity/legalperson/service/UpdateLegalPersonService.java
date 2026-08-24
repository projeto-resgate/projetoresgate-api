package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.UpdateLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.UpdateLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
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

        String cnpj = command.cnpj();
        if (nonNull(cnpj) && !cnpj.isBlank() && repository.existsByCnpjAndIdNot(cnpj, person.getId())) {
            throw new InternalException("Já existe uma empresa cadastrada com este CNPJ.");
        }

        return repository.save(
                person.update()
                        .cnpj(command.cnpj())
                        .corporateName(command.corporateName())
                        .tradeName(command.tradeName())
                        .displayName(command.displayName())
                        .mainCnaeCode(command.mainCnaeCode())
                        .registrationStatus(command.registrationStatus())
                        .companyStatus(command.companyStatus())
                        .address(command.address())
                        .representative(command.representative())
                        .apply()
        );
    }
}
