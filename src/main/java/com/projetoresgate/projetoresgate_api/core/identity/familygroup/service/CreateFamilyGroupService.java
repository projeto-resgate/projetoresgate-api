package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.address.usecase.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.CreateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.CreateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class CreateFamilyGroupService implements CreateFamilyGroupUseCase {

    private final FamilyGroupRepository repository;

    public CreateFamilyGroupService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FamilyGroup handle(CreateFamilyGroupCommand command) {
        if (isNull(command)) {
            throw new InternalException("O comando não pode ser nulo.");
        }

        if (nonNull(command.name()) && !command.name().isBlank() && repository.existsByName(command.name())) {
            throw new InternalException("Já existe um grupo familiar cadastrado com este nome.");
        }

        String friendlyId = generateFriendlyId();

        Address address = toAddress(command.address());

        FamilyGroup familyGroup = FamilyGroup.create(
                friendlyId,
                command.name(),
                command.householdIncome(),
                command.perCapitaIncome(),
                command.educationExpense(),
                command.healthExpense(),
                command.housingExpense(),
                command.numberOfResidents(),
                address
        );

        return repository.save(familyGroup);
    }

    private String generateFriendlyId() {
        return FamilyGroup.FRIENDLY_ID_PREFIX + repository.nextFriendlyIdValue();
    }

    private Address toAddress(AddressCommand addressCommand) {
        if (isNull(addressCommand)) {
            return null;
        }
        return Address.create(
                addressCommand.streetType(),
                addressCommand.streetName(),
                addressCommand.zipCode(),
                addressCommand.number(),
                addressCommand.referencePoint(),
                addressCommand.complement(),
                addressCommand.neighborhood(),
                addressCommand.city(),
                addressCommand.state()
        );
    }
}
