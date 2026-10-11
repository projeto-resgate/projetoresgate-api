package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.address.usecase.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.UpdateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class UpdateFamilyGroupService implements UpdateFamilyGroupUseCase {

    private final FamilyGroupRepository repository;

    public UpdateFamilyGroupService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FamilyGroup handle(UpdateFamilyGroupCommand command) {
        if (isNull(command)) {
            throw new InternalException("O comando não pode ser nulo.");
        }

        FamilyGroup existing = repository.findByIdOrThrow(command.id());

        if (nonNull(command.name()) && !command.name().isBlank()
                && repository.existsByNameAndIdNot(command.name(), command.id())) {
            throw new InternalException("Já existe um grupo familiar cadastrado com este nome.");
        }

        Address address = toAddress(command.address(), existing.getAddress());

        FamilyGroup updated = existing.update()
                .name(command.name())
                .householdIncome(command.householdIncome())
                .perCapitaIncome(command.perCapitaIncome())
                .educationExpense(command.educationExpense())
                .healthExpense(command.healthExpense())
                .housingExpense(command.housingExpense())
                .numberOfResidents(command.numberOfResidents())
                .address(address)
                .apply();

        return repository.save(updated);
    }

    private Address toAddress(AddressCommand addressCommand, Address existingAddress) {
        if (isNull(addressCommand)) {
            return existingAddress;
        }
        if (nonNull(existingAddress)) {
            existingAddress.update()
                    .streetType(addressCommand.streetType())
                    .streetName(addressCommand.streetName())
                    .zipCode(addressCommand.zipCode())
                    .number(addressCommand.number())
                    .referencePoint(addressCommand.referencePoint())
                    .complement(addressCommand.complement())
                    .neighborhood(addressCommand.neighborhood())
                    .city(addressCommand.city())
                    .state(addressCommand.state())
                    .apply();
            return existingAddress;
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
