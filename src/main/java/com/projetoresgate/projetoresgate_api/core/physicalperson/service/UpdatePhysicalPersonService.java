package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Rg;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.UpdatePhysicalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.UpdatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdatePhysicalPersonService implements UpdatePhysicalPersonUseCase {

    private final PhysicalPersonRepository repository;

    public UpdatePhysicalPersonService(PhysicalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public PhysicalPerson handle(UpdatePhysicalPersonCommand command) {
        PhysicalPerson person = repository.findByIdOrThrow(command.id());

        Cpf cpf = new Cpf(command.cpf());
        Rg rg = new Rg(command.rg());

        if (!person.getCpf().equals(cpf) && repository.existsByCpf(cpf)) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        person.updatePersonalInfo(
                rg,
                cpf,
                command.birthDate(),
                command.gender(),
                command.phone(),
                command.cellphone()
        );

        return repository.save(person);
    }
}
