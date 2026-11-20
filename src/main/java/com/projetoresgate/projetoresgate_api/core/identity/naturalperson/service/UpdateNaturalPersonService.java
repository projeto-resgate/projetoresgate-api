package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
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
    private final LegalPersonRepository legalPersonRepository;

    public UpdateNaturalPersonService(NaturalPersonRepository repository,
                                      LegalPersonRepository legalPersonRepository) {
        this.repository = repository;
        this.legalPersonRepository = legalPersonRepository;
    }

    @Override
    @Transactional
    public NaturalPerson handle(UpdateNaturalPersonCommand command) {
        NaturalPerson person = repository.findByIdOrThrow(command.id());

        String cpf = command.cpf();
        if (nonNull(cpf) && !cpf.isBlank() && repository.existsByCpfAndIdNot(cpf, person.getId())) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        if (nonNull(command.workplaceId()) && !command.workplaceId().equals(person.getWorkplaceId())) {
            legalPersonRepository.findByIdOrThrow(command.workplaceId());
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
                        .workplaceId(command.workplaceId())
                        .apply()
        );
    }
}
