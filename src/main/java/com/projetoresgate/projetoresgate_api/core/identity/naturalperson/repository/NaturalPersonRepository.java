package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository;

import java.util.List;

import java.util.Collection;

import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NaturalPersonRepository extends JpaRepository<NaturalPerson, UUID>, JpaSpecificationExecutor<NaturalPerson> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);

    boolean existsByRg(String rg);

    boolean existsByRgAndIdNot(String rg, UUID id);

    List<NaturalPerson> findAllByEmailIgnoreCase(String email);

    default NaturalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada com ID: " + id));
    }

    @Query(value = "SELECT * FROM natural_person WHERE id IN (:ids)", nativeQuery = true)
    List<NaturalPerson> findAllByIdIncludingDeleted(@Param("ids") Collection<UUID> ids);

    default Optional<NaturalPerson> findByIdIncludingDeleted(UUID id) {
        return findAllByIdIncludingDeleted(List.of(id)).stream().findFirst();
    }

    default NaturalPerson findByIdIncludingDeletedOrThrow(UUID id) {
        return findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada com ID: " + id));
    }
}
