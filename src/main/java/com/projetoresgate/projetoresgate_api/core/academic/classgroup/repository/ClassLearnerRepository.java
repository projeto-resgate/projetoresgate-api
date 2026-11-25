package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassLearner;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassLearnerRepository extends JpaRepository<ClassLearner, UUID>,
        JpaSpecificationExecutor<ClassLearner> {

    Optional<ClassLearner> findByIdAndClassGroupId(UUID id, UUID classGroupId);

    default ClassLearner findByIdAndClassGroupIdOrThrow(UUID id, UUID classGroupId) {
        return findByIdAndClassGroupId(id, classGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Educando não encontrado com ID: " + id + " na turma: " + classGroupId));
    }

    Optional<ClassLearner> findByClassGroupIdAndLearnerId(UUID classGroupId, UUID learnerId);

    boolean existsByClassGroupIdAndLearnerId(UUID classGroupId, UUID learnerId);

    @Query(value = """
            select cl.id as "id",
                   cl.class_group_id as "classGroupId",
                   cl.learner_id as "learnerId",
                   np.name as "learnerName",
                   np.cpf as "learnerCpf",
                   l.status as "learnerStatus",
                   cl.active as "active",
                   cl.date_created as "dateCreated"
            from class_learner cl
            join learner l on l.id = cl.learner_id and l.deleted_at is null
            join natural_person np on np.id = l.natural_person_id
            where cl.class_group_id = :classGroupId
              and cl.deleted_at is null
              and (cast(:active as boolean) is null or cl.active = cast(:active as boolean))
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0)
            order by cl.active desc, np.name asc, cl.id asc
            """,
            countQuery = """
            select count(*)
            from class_learner cl
            join learner l on l.id = cl.learner_id and l.deleted_at is null
            join natural_person np on np.id = l.natural_person_id
            where cl.class_group_id = :classGroupId
              and cl.deleted_at is null
              and (cast(:active as boolean) is null or cl.active = cast(:active as boolean))
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0)
            """,
            nativeQuery = true)
    Page<ClassLearnerRow> findRowsByClassGroupId(@Param("classGroupId") UUID classGroupId,
                                                 @Param("active") Boolean active,
                                                 @Param("searchTerm") String searchTerm,
                                                 Pageable pageable);

    @Query("""
            select distinct cl.learnerId from ClassLearner cl
            where cl.learnerId in :learnerIds
              and cl.active = true
              and cl.classGroup.active = true
              and cl.classGroup.startDate <= :now
              and cl.classGroup.endDate >= :now
            """)
    List<UUID> findLearnerIdsInProgress(@Param("learnerIds") Collection<UUID> learnerIds,
                                        @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            update ClassLearner cl
            set cl.active = false
            where cl.learnerId = :learnerId and cl.active = true
            """)
    int deactivateByLearnerId(@Param("learnerId") UUID learnerId);

    @Modifying
    @Query(value = """
            update class_learner
            set deleted_at = now()
            where class_group_id = :classGroupId and deleted_at is null
            """, nativeQuery = true)
    void softDeleteByClassGroupId(@Param("classGroupId") UUID classGroupId);
}
