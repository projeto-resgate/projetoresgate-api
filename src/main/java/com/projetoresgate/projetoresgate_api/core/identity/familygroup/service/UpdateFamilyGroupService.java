package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.address.api.command.AddressCommand;
import com.projetoresgate.projetoresgate_api.core.identity.address.domain.Address;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.UpdateFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.UpdateFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        FamilyGroup familyGroup = repository.findByIdOrThrow(command.id());

        if (nonNull(command.name()) && !command.name().isBlank()
                && repository.existsByNameAndIdNot(command.name(), familyGroup.getId())) {
            throw new InternalException("Já existe um grupo familiar cadastrado com este nome.");
        }

        updateAddress(familyGroup, command.address());

        return repository.save(
                familyGroup.update()
                        .name(command.name())
                        .householdIncome(command.householdIncome())
                        .perCapitaIncome(command.perCapitaIncome())
                        .educationExpense(command.educationExpense())
                        .healthExpense(command.healthExpense())
                        .housingExpense(command.housingExpense())
                        .numberOfResidents(command.numberOfResidents())
                        .apply()
        );
    }

    private void updateAddress(FamilyGroup familyGroup, AddressCommand command) {
        if (command == null) {
            return;
        }

        Address current = nonNull(familyGroup.getAddress())
                ? familyGroup.getAddress()
                : Address.create(command.streetType(), command.streetName(), command.zipCode(), command.number(),
                command.referencePoint(), command.complement(), command.neighborhood(), command.city(),
                command.state());
        current.update()
                .streetType(command.streetType())
                .streetName(command.streetName())
                .zipCode(command.zipCode())
                .number(command.number())
                .referencePoint(command.referencePoint())
                .complement(command.complement())
                .neighborhood(command.neighborhood())
                .city(command.city())
                .state(command.state())
                .apply();

        if (!nonNull(familyGroup.getAddress())) {
            familyGroup.update().address(current).apply();
        }
    }
}
