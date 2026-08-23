package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.UpdateNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class UpdateNaturalPersonService implements UpdateNaturalPersonUseCase {

    private final NaturalPersonRepository repository;

    public UpdateNaturalPersonService(NaturalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public NaturalPerson handle(UpdateNaturalPersonCommand command) {
        NaturalPerson person = repository.findByIdOrThrow(command.id());

        String cpf = command.cpf();
        if (nonNull(cpf) && !cpf.isBlank() && repository.existsByCpfAndIdNot(cpf, person.getId())) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        return repository.save(
                person.update()
                        .name(command.name())
                        .email(command.email())
                        .nickname(command.nickname())
                        .cpf(command.cpf())
                        .rg(command.rg())
                        .birthDate(command.birthDate())
                        .gender(command.gender())
                        .phone(command.phone())
                        .cellphone(command.cellphone())
                        .address(command.address())
                        .apply()
        );
    }
}
