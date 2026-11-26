package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.usecase;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Id de educando de cada pessoa física que é educanda. Implementado no módulo acadêmico, para que
 * {@code identity} não dependa de {@code academic}.
 */
public interface FindLearnerIdsByNaturalPersonIdsUseCase {

    Map<UUID, UUID> handle(Collection<UUID> naturalPersonIds);
}
