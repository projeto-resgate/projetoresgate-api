package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
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
public interface ClassCollaboratorRepository extends JpaRepository<ClassCollaborator, UUID>,
        JpaSpecificationExecutor<ClassCollaborator> {

    Optional<ClassCollaborator> findByIdAndClassGroupId(UUID id, UUID classGroupId);

    default ClassCollaborator findByIdAndClassGroupIdOrThrow(UUID id, UUID classGroupId) {
        return findByIdAndClassGroupId(id, classGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Educador não encontrado com ID: " + id + " na turma: " + classGroupId));
    }

    boolean existsByClassGroupIdAndCollaboratorId(UUID classGroupId, UUID collaboratorId);

    @Query(value = """
            select c from ClassCollaborator c
            join Collaborator col on col.id = c.collaboratorId
            join NaturalPerson np on np.id = col.naturalPersonId
            where c.classGroup.id = :classGroupId
              and (:active is null or c.active = :active)
              and (:educatorCategoryItemId is null or c.educatorCategoryItemId = :educatorCategoryItemId)
              and (:searchTerm is null
                   or lower(np.name) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or lower(np.nickname) like lower(concat('%', cast(:searchTerm as String), '%')))
            order by c.active desc, np.name asc
            """,
            countQuery = """
                    select count(c) from ClassCollaborator c
                    join Collaborator col on col.id = c.collaboratorId
                    join NaturalPerson np on np.id = col.naturalPersonId
                    where c.classGroup.id = :classGroupId
                      and (:active is null or c.active = :active)
                      and (:educatorCategoryItemId is null or c.educatorCategoryItemId = :educatorCategoryItemId)
                      and (:searchTerm is null
                           or lower(np.name) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or lower(np.nickname) like lower(concat('%', cast(:searchTerm as String), '%')))
                    """)
    Page<ClassCollaborator> findAllByClassGroupId(@Param("classGroupId") UUID classGroupId,
                                                  @Param("active") Boolean active,
                                                  @Param("educatorCategoryItemId") UUID educatorCategoryItemId,
                                                  @Param("searchTerm") String searchTerm,
                                                  Pageable pageable);

    @Modifying
    @Query(value = """
            update class_collaborator
            set deleted_at = now()
            where class_group_id = :classGroupId and deleted_at is null
            """, nativeQuery = true)
    void softDeleteByClassGroupId(@Param("classGroupId") UUID classGroupId);
}
