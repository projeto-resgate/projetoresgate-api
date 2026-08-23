package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.CreateNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class CreateNaturalPersonService implements CreateNaturalPersonUseCase {

    private final NaturalPersonRepository repository;
    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;

    public CreateNaturalPersonService(NaturalPersonRepository repository,
                                      RequestEmailConfirmationUseCase requestEmailConfirmationUseCase) {
        this.repository = repository;
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
    }

    @Override
    @Transactional
    public NaturalPerson handle(CreateNaturalPersonCommand command) {
        String cpf = command.cpf();

        if (nonNull(cpf) && !cpf.isBlank() && repository.existsByCpf(cpf)) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        NaturalPerson person = NaturalPerson.create(
                command.name(),
                command.email(),
                command.nickname(),
                cpf,
                command.rg(),
                command.birthDate(),
                command.gender(),
                command.phone(),
                command.cellphone(),
                command.address()
        );

        NaturalPerson saved = repository.save(person);

        requestEmailConfirmationUseCase.handle(saved.getEmail());

        return saved;
    }
}
