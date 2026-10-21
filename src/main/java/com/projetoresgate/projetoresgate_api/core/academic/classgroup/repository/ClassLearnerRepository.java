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

    boolean existsByClassGroupIdAndLearnerId(UUID classGroupId, UUID learnerId);

    @Query(value = """
            select cl from ClassLearner cl
            join Learner l on l.id = cl.learnerId
            join NaturalPerson np on np.id = l.naturalPersonId
            where cl.classGroup.id = :classGroupId
              and (:active is null or cl.active = :active)
              and (:searchTerm is null
                   or lower(np.name) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or lower(np.nickname) like lower(concat('%', cast(:searchTerm as String), '%')))
            order by cl.active desc, np.name asc
            """,
            countQuery = """
                    select count(cl) from ClassLearner cl
                    join Learner l on l.id = cl.learnerId
                    join NaturalPerson np on np.id = l.naturalPersonId
                    where cl.classGroup.id = :classGroupId
                      and (:active is null or cl.active = :active)
                      and (:searchTerm is null
                           or lower(np.name) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or lower(np.nickname) like lower(concat('%', cast(:searchTerm as String), '%')))
                    """)
    Page<ClassLearner> findAllByClassGroupId(@Param("classGroupId") UUID classGroupId,
                                             @Param("active") Boolean active,
                                             @Param("searchTerm") String searchTerm,
                                             Pageable pageable);

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
