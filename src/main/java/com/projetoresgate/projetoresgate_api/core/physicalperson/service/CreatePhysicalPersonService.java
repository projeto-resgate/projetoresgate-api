package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Rg;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.CreatePhysicalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.CreatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import com.projetoresgate.projetoresgate_api.core.user.usecase.CreateUserUseCase;
import com.projetoresgate.projetoresgate_api.core.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePhysicalPersonService implements CreatePhysicalPersonUseCase {

    private final PhysicalPersonRepository repository;
    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;

    public CreatePhysicalPersonService(PhysicalPersonRepository repository,
                                       UserRepository userRepository,
                                       CreateUserUseCase createUserUseCase) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.createUserUseCase = createUserUseCase;
    }

    @Override
    @Transactional
    public PhysicalPerson handle(CreatePhysicalPersonCommand command) {
        Cpf cpf = new Cpf(command.cpf());
        Rg rg = new Rg(command.rg());

        if (repository.existsByCpf(cpf)) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        CreateUserCommand createUserCommand = new CreateUserCommand(
                command.name(),
                command.email(),
                command.nickname(),
                null
        );

        User newUser = createUserUseCase.handle(createUserCommand);
        
        newUser.addRole(UserRole.PHYSICAL_PERSON);
        
        userRepository.save(newUser);

        PhysicalPerson person = PhysicalPerson.create(
                newUser,
                cpf,
                rg,
                command.birthDate(),
                command.gender(),
                command.phone(),
                command.cellphone()
        );

        return repository.save(person);
    }
}
