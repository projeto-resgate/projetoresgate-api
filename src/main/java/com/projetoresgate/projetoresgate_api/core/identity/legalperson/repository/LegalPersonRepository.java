package com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository;

import java.util.Optional;

import java.util.List;

import java.util.Collection;

import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LegalPersonRepository extends JpaRepository<LegalPerson, UUID>, JpaSpecificationExecutor<LegalPerson> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, UUID id);

    default LegalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));
    }

    @Query(value = "SELECT * FROM legal_person WHERE id IN (:ids)", nativeQuery = true)
    List<LegalPerson> findAllByIdIncludingDeleted(@Param("ids") Collection<UUID> ids);

    default Optional<LegalPerson> findByIdIncludingDeleted(UUID id) {
        return findAllByIdIncludingDeleted(List.of(id)).stream().findFirst();
    }

    default LegalPerson findByIdIncludingDeletedOrThrow(UUID id) {
        return findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));
    }
}
