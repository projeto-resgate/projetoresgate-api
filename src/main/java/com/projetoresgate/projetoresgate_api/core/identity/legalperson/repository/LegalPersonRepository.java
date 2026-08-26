package com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LegalPersonRepository extends JpaRepository<LegalPerson, UUID>, JpaSpecificationExecutor<LegalPerson> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, UUID id);

    default LegalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));
    }
}
