package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassGroup;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            select cg.id as "id",
                   cg.title as "title",
                   cg.program_id as "programId",
                   p.name as "programName",
                   cg.institution_id as "institutionId",
                   coalesce(nullif(trim(lp.display_name), ''), nullif(trim(lp.trade_name), ''), lp.corporate_name) as "institutionName",
                   cg.active as "active"
            from class_group cg
            left join program p on p.id = cg.program_id
            left join legal_person lp on lp.id = cg.institution_id
            where cg.deleted_at is null
              and (cast(:searchTerm as text) is null or strpos(lower(cg.title), lower(cast(:searchTerm as text))) > 0)
              and (cast(:programId as uuid) is null or cg.program_id = cast(:programId as uuid))
              and (cast(:institutionId as uuid) is null or cg.institution_id = cast(:institutionId as uuid))
              and (cast(:active as boolean) is null or cg.active = cast(:active as boolean))
            order by cg.title asc, cg.id asc
            """,
            countQuery = """
            select count(*)
            from class_group cg
            where cg.deleted_at is null
              and (cast(:searchTerm as text) is null or strpos(lower(cg.title), lower(cast(:searchTerm as text))) > 0)
              and (cast(:programId as uuid) is null or cg.program_id = cast(:programId as uuid))
              and (cast(:institutionId as uuid) is null or cg.institution_id = cast(:institutionId as uuid))
              and (cast(:active as boolean) is null or cg.active = cast(:active as boolean))
            """,
            nativeQuery = true)
    Page<ClassGroupRow> findRows(@Param("searchTerm") String searchTerm,
                                 @Param("programId") UUID programId,
                                 @Param("institutionId") UUID institutionId,
                                 @Param("active") Boolean active,
                                 Pageable pageable);

    interface ClassGroupDependencies {
        long getLearners();

        long getCollaborators();
    }

    @Query(value = """
            select (select count(*) from class_learner
                    where class_group_id = :id and deleted_at is null) as "learners",
                   (select count(*) from class_collaborator
                    where class_group_id = :id and deleted_at is null) as "collaborators"
            """, nativeQuery = true)
    ClassGroupDependencies countDependencies(@Param("id") UUID id);
}
