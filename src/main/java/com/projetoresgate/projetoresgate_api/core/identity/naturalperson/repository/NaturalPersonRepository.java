package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NaturalPersonRepository extends JpaRepository<NaturalPerson, UUID>, JpaSpecificationExecutor<NaturalPerson> {

    Optional<NaturalPerson> findByUserId(UUID userId);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);

    default NaturalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada com ID: " + id));
    }

    default NaturalPerson findByUserIdOrThrow(UUID userId) {
        return findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada para o usuário com ID: " + userId));
    }
}
