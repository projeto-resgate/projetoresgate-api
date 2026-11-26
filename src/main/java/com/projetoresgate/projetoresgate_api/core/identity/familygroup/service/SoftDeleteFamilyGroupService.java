package com.projetoresgate.projetoresgate_api.core.identity.familygroup.service;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository.FamilyGroupRepository;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.SoftDeleteFamilyGroupUseCase;
import com.projetoresgate.projetoresgate_api.core.identity.familygroup.usecase.command.SoftDeleteFamilyGroupCommand;
import com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.projetoresgate.projetoresgate_api.shared.deletion.DeletionGuard.Dependency.of;

@Service
public class SoftDeleteFamilyGroupService implements SoftDeleteFamilyGroupUseCase {

    private final FamilyGroupRepository repository;

    public SoftDeleteFamilyGroupService(FamilyGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void handle(SoftDeleteFamilyGroupCommand command) {
        FamilyGroup familyGroup = repository.findByIdOrThrow(command.id());

        DeletionGuard.ensureNoDependencies("o grupo familiar",
                of(repository.countActiveMembers(familyGroup.getId()), "pessoa física", "pessoas físicas"));
        repository.softDeleteAddressOf(familyGroup.getId());
        repository.softDeleteKeepingMembers(familyGroup.getId());
    }
}
