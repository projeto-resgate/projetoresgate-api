package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.service;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository.NaturalPersonRoleRepository.NaturalPersonRoleProjection;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase.FindNaturalPersonRolesUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Objects.isNull;

@Service
public class FindNaturalPersonRolesService implements FindNaturalPersonRolesUseCase {

    private final NaturalPersonRoleRepository repository;

    public FindNaturalPersonRolesService(NaturalPersonRoleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<NaturalPersonRoleType>> handle(Collection<UUID> naturalPersonIds) {
        if (isNull(naturalPersonIds) || naturalPersonIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Set<NaturalPersonRoleType>> grouped = new HashMap<>();
        for (NaturalPersonRoleProjection projection : repository.findRolesByNaturalPersonIdsIn(naturalPersonIds)) {
            grouped.computeIfAbsent(projection.getNaturalPersonId(),
                    key -> EnumSet.noneOf(NaturalPersonRoleType.class)).add(projection.getRole());
        }

        Map<UUID, List<NaturalPersonRoleType>> rolesByNaturalPerson = new HashMap<>();
        grouped.forEach((naturalPersonId, roles) -> {
            List<NaturalPersonRoleType> sorted = new ArrayList<>(roles);
            sorted.sort(Enum::compareTo);
            rolesByNaturalPerson.put(naturalPersonId, List.copyOf(sorted));
        });

        return rolesByNaturalPerson;
    }
}