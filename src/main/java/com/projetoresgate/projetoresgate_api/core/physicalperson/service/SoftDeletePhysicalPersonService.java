package com.projetoresgate.projetoresgate_api.core.physicalperson.service;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.repository.PhysicalPersonRepository;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.SoftDeletePhysicalPersonUseCase;
import com.projetoresgate.projetoresgate_api.core.physicalperson.usecase.command.SoftDeletePhysicalPersonCommand;
import com.projetoresgate.projetoresgate_api.core.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SoftDeletePhysicalPersonService implements SoftDeletePhysicalPersonUseCase {

    private final PhysicalPersonRepository physicalPersonRepository;
    private final UserRepository userRepository;

    public SoftDeletePhysicalPersonService(PhysicalPersonRepository physicalPersonRepository, UserRepository userRepository) {
        this.physicalPersonRepository = physicalPersonRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void handle(SoftDeletePhysicalPersonCommand command) {
        PhysicalPerson person = physicalPersonRepository.findByIdOrThrow(command.id());
        userRepository.delete(person.getUser());
        physicalPersonRepository.delete(person);
    }
}
