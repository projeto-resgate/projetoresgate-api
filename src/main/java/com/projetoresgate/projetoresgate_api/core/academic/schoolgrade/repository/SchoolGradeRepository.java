package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchoolGradeRepository extends JpaRepository<SchoolGrade, UUID>,
        JpaSpecificationExecutor<SchoolGrade> {

    default SchoolGrade findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Série escolar não encontrada com ID: " + id));
    }

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
