package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Cpf;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.vo.Rg;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.UpdatePhysicalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.UpdatePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.domain.User;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static java.util.Objects.nonNull;

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

        updateUserData(person.getUser(), command);

        if (StringUtils.hasText(command.cpf())) {
            Cpf cpf = new Cpf(command.cpf());
            if (!person.getCpf().equals(cpf) && repository.existsByCpfAndIdNot(cpf, command.id())) {
                throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
            }
            person.setCpf(cpf);
        }

        if (StringUtils.hasText(command.rg())) {
            person.setRg(new Rg(command.rg()));
        }

        if (nonNull(command.birthDate())) {
            person.setBirthDate(command.birthDate());
        }

        if (nonNull(command.gender())) {
            person.setGender(command.gender());
        }

        if (nonNull(command.phone())) {
            person.setPhone(command.phone());
        }

        if (nonNull(command.cellphone())) {
            person.setCellphone(command.cellphone());
        }

        person.validate();
        return repository.save(person);
    }

    private void updateUserData(User user, UpdatePhysicalPersonCommand command) {
        if (user == null) return;

        String name = StringUtils.hasText(command.name()) ? command.name() : user.getName();
        String nickname = command.nickname() != null ? command.nickname() : user.getNickname();

        user.updateInfo(name, nickname);
    }
}
