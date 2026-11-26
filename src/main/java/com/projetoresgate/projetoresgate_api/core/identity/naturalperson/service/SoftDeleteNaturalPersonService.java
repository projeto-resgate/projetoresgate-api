package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.SoftDeleteNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard.Dependency.of;

@Service
public class SoftDeleteNaturalPersonService implements SoftDeleteNaturalPersonUseCase {

    private final NaturalPersonRepository naturalPersonRepository;

    public SoftDeleteNaturalPersonService(NaturalPersonRepository naturalPersonRepository) {
        this.naturalPersonRepository = naturalPersonRepository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteNaturalPersonCommand command) {
        NaturalPerson person = naturalPersonRepository.findByIdOrThrow(command.id());

        NaturalPersonRepository.NaturalPersonDependencies dependencies =
                naturalPersonRepository.countDependencies(person.getId());
        DeletionGuard.ensureNoDependencies("a pessoa física",
                of(dependencies.getLearners(), "cadastro de educando", "cadastros de educando"),
                of(dependencies.getResponsibilities(), "educando como responsável", "educandos como responsável"),
                of(dependencies.getCollaborators(), "cadastro de colaborador", "cadastros de colaborador"),
                of(dependencies.getFamilyGroups(), "grupo familiar", "grupos familiares"));

        naturalPersonRepository.delete(person);
    }
}
