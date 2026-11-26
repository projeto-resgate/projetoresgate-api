package com.projetoresgate.projetoresgate_api.core.identity.legalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository.LegalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.SoftDeleteLegalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.legalperson.usecase.command.SoftDeleteLegalPersonCommand;
import com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard.Dependency.of;

@Service
public class SoftDeleteLegalPersonService implements SoftDeleteLegalPersonUseCase {

    private final LegalPersonRepository repository;

    public SoftDeleteLegalPersonService(LegalPersonRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteLegalPersonCommand command) {
        LegalPerson person = repository.findByIdOrThrow(command.id());

        LegalPersonRepository.LegalPersonDependencies dependencies = repository.countDependencies(person.getId());
        DeletionGuard.ensureNoDependencies("a pessoa jurídica",
                of(dependencies.getClassGroups(), "turma", "turmas"),
                of(dependencies.getPrograms(), "programa", "programas"),
                of(dependencies.getCollaborators(), "colaborador", "colaboradores"),
                of(dependencies.getLearners(), "educando", "educandos"),
                of(dependencies.getWorkplaces(), "pessoa física (local de trabalho)", "pessoas físicas (local de trabalho)"));

        repository.delete(person);
    }
}