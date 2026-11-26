package com.projetoresgate.projetoresgate_api.core.academic.program.repository;




import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;

import com.projetoresgate.projetoresgate_api.core.academic.program.domain.Program;
import com.projetoresgate.projetoresgate_api.core.academic.program.domain.enums.ProgramStatus;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID>, JpaSpecificationExecutor<Program> {

    default Program findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa não encontrado com ID: " + id));
    }

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    boolean existsByInstitutionId(UUID institutionId);

    boolean existsByStatus(ProgramStatus status);

    @Query(value = """
            select count(*) from class_group
            where program_id = :id and deleted_at is null
            """, nativeQuery = true)
    long countActiveClassGroups(@Param("id") UUID id);
}
