package com.projetoresgate.projetoresgate_api.core.academic.learner.repository;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerRepository extends JpaRepository<Learner, UUID>, JpaSpecificationExecutor<Learner> {

    boolean existsByNaturalPersonId(UUID naturalPersonId);

    Optional<Learner> findByNaturalPersonId(UUID naturalPersonId);

    default Learner findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Educando não encontrado com ID: " + id));
    }
}