package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.SoftDeleteNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        naturalPersonRepository.delete(person);
    }
}
