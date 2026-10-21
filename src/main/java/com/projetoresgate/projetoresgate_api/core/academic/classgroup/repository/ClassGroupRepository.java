package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup, UUID>, JpaSpecificationExecutor<ClassGroup> {

    default ClassGroup findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + id));
    }

    @Query("select cg from ClassGroup cg where cg.programId = :programId")
    List<ClassGroup> findByProgramId(@Param("programId") UUID programId);

    @Query("select distinct cg from ClassGroup cg join cg.schoolGradeList sg where sg.id = :schoolGradeId")
    List<ClassGroup> findBySchoolGradeId(@Param("schoolGradeId") UUID schoolGradeId);

    boolean existsByProgramId(UUID programId);

    boolean existsByInstitutionId(UUID institutionId);
}
