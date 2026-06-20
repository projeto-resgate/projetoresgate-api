package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.SoftDeleteNaturalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.command.SoftDeleteNaturalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.identity.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeleteNaturalPersonService implements SoftDeleteNaturalPersonUseCase {

    private final NaturalPersonRepository naturalPersonRepository;
    private final UserRepository userRepository;

    public SoftDeleteNaturalPersonService(NaturalPersonRepository naturalPersonRepository, UserRepository userRepository) {
        this.naturalPersonRepository = naturalPersonRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteNaturalPersonCommand command) {
        NaturalPerson person = naturalPersonRepository.findByIdOrThrow(command.id());
        userRepository.delete(person.getUser());
        naturalPersonRepository.delete(person);
    }
}
