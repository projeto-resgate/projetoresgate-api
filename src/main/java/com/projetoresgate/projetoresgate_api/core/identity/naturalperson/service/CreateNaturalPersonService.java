package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.CreateNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.CreateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.User;
import com.projetoresgate.projetoresgate_api.core.identity.user.domain.enums.UserRole;
import com.projetoresgate.projetoresgate_api.core.identity.user.service.UserRegistrationService;
import com.projetoresgate.projetoresgate_api.core.identity.user.usecase.command.CreateUserCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateNaturalPersonService implements CreateNaturalPersonUseCase {

    private final NaturalPersonRepository repository;
    private final UserRegistrationService userRegistrationService;

    public CreateNaturalPersonService(NaturalPersonRepository repository,
                                      UserRegistrationService userRegistrationService) {
        this.repository = repository;
        this.userRegistrationService = userRegistrationService;
    }

    @Override
    @Transactional
    public NaturalPerson handle(CreateNaturalPersonCommand command) {
        String cpf = command.cpf();

        if (repository.existsByCpf(cpf)) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        CreateUserCommand createUserCommand = new CreateUserCommand(
                command.name(),
                command.email(),
                command.nickname(),
                "12345678" //TODO: Senha temporária, no futuro os cadastros de administrador e secretária terão fluxo para criação de senha.
        );

        User newUser = userRegistrationService.registerNewUser(createUserCommand, UserRole.NATURAL_PERSON);

        NaturalPerson person = NaturalPerson.create(
                newUser,
                cpf,
                command.rg(),
                command.birthDate(),
                command.gender(),
                command.phone(),
                command.cellphone()
        );

        return repository.save(person);
    }
}