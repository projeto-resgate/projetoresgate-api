package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FindNaturalPersonRolesUseCase {

    Map<UUID, List<NaturalPersonRoleType>> handle(Collection<UUID> naturalPersonIds);
}