package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroupSequence;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupSequenceRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.CreateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;

@Service
public class CreateFamilyGroupService implements CreateFamilyGroupUseCase {

    private final FamilyGroupRepository repository;
    private final FamilyGroupSequenceRepository sequenceRepository;

    public CreateFamilyGroupService(FamilyGroupRepository repository,
                                    FamilyGroupSequenceRepository sequenceRepository) {
        this.repository = repository;
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public FamilyGroup handle(CreateFamilyGroupCommand command) {
        if (nonNull(command.name()) && !command.name().isBlank() && repository.existsByName(command.name())) {
            throw new InternalException("Já existe um grupo familiar cadastrado com este nome.");
        }

        FamilyGroup familyGroup = FamilyGroup.create(
                nextFriendlyId(),
                command.name(),
                command.householdIncome(),
                command.perCapitaIncome(),
                command.educationExpense(),
                command.healthExpense(),
                command.housingExpense(),
                command.numberOfResidents(),
                toAddress(command.address())
        );

        return repository.save(familyGroup);
    }

    private String nextFriendlyId() {
        FamilyGroupSequence sequence = sequenceRepository
                .findByIdForUpdate(FamilyGroupSequence.SINGLETON_ID)
                .orElseGet(() -> sequenceRepository.save(FamilyGroupSequence.initial()));

        return FamilyGroup.FRIENDLY_ID_PREFIX + sequence.nextValue();
    }

    private Address toAddress(AddressCommand command) {
        if (command == null) {
            return null;
        }

        return Address.create(
                command.streetType(),
                command.streetName(),
                command.zipCode(),
                command.number(),
                command.referencePoint(),
                command.complement(),
                command.neighborhood(),
                command.city(),
                command.state()
        );
    }
}
