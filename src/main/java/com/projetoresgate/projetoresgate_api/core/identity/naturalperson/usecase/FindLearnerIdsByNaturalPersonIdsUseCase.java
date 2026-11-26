package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface FindLearnerIdsByNaturalPersonIdsUseCase {

    Map<UUID, UUID> handle(Collection<UUID> naturalPersonIds);
}
