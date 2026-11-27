package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.EmailConfirmationTokenRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.RequestEmailConfirmationUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.UpdateNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.UpdateNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class UpdateNaturalPersonService implements UpdateNaturalPersonUseCase {

    private final NaturalPersonRepository repository;
    private final LegalPersonRepository legalPersonRepository;
    private final RequestEmailConfirmationUseCase requestEmailConfirmationUseCase;
    private final EmailConfirmationTokenRepository emailConfirmationTokenRepository;

    public UpdateNaturalPersonService(NaturalPersonRepository repository,
                                      LegalPersonRepository legalPersonRepository,
                                      RequestEmailConfirmationUseCase requestEmailConfirmationUseCase,
                                      EmailConfirmationTokenRepository emailConfirmationTokenRepository) {
        this.repository = repository;
        this.legalPersonRepository = legalPersonRepository;
        this.requestEmailConfirmationUseCase = requestEmailConfirmationUseCase;
        this.emailConfirmationTokenRepository = emailConfirmationTokenRepository;
    }

    @Override
    @Transactional
    public NaturalPerson handle(UpdateNaturalPersonCommand command) {
        NaturalPerson person = repository.findByIdOrThrow(command.id());

        String cpf = command.cpf();
        if (nonNull(cpf) && !cpf.isBlank() && repository.existsByCpfAndIdNot(cpf, person.getId())) {
            throw new InternalException("Já existe uma pessoa cadastrada com este CPF.");
        }

        String rg = command.rg();
        if (nonNull(rg) && !rg.isBlank() && repository.existsByRgAndIdNot(rg, person.getId())) {
            throw new InternalException("Já existe uma pessoa cadastrada com este RG.");
        }

        if (nonNull(command.workplaceId()) && !command.workplaceId().equals(person.getWorkplaceId())) {
            legalPersonRepository.findByIdOrThrow(command.workplaceId());
        }

        String previousEmail = person.getEmail();

        NaturalPerson saved = repository.save(
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

        if (nonNull(saved.getEmail()) && !saved.getEmail().equalsIgnoreCase(previousEmail)) {
            requestEmailConfirmationUseCase.handle(saved);
        } else if (isNull(saved.getEmail()) && nonNull(previousEmail)) {
            emailConfirmationTokenRepository.deleteByNaturalPerson(saved);
        }

        return saved;
    }
}
