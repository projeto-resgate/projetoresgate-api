package com.projetoresgate.projetoresgate_api.core.physicalperson.repository;

import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.PhysicalPerson;
import com.projetoresgate.projetoresgate_api.core.physicalperson.domain.valueobjects.Cpf;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhysicalPersonRepository extends JpaRepository<PhysicalPerson, UUID>, JpaSpecificationExecutor<PhysicalPerson> {

    Optional<PhysicalPerson> findByUserId(UUID userId);

    boolean existsByCpf(Cpf cpf);

    default PhysicalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada com ID: " + id));
    }
    
    default PhysicalPerson findByUserIdOrThrow(UUID userId) {
        return findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada para o usuário com ID: " + userId));
    }
}
