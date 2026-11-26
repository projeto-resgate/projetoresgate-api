package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassCollaborator;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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

    boolean existsByEducatorCategoryItemId(UUID educatorCategoryItemId);

    @Query(value = """
            select c.id as "id",
                   c.class_group_id as "classGroupId",
                   c.collaborator_id as "collaboratorId",
                   np.name as "collaboratorName",
                   np.cpf as "collaboratorCpf",
                   np.rg as "collaboratorRg",
                   c.educator_category_item_id as "educatorCategoryItemId",
                   eci.name as "educatorCategoryItemName",
                   c.active as "active",
                   c.date_created as "dateCreated"
            from class_collaborator c
            join collaborator col on col.id = c.collaborator_id
            join natural_person np on np.id = col.natural_person_id
            left join educator_category_item eci on eci.id = c.educator_category_item_id
            where c.class_group_id = :classGroupId
              and c.deleted_at is null
              and (cast(:active as boolean) is null or c.active = cast(:active as boolean))
              and (cast(:educatorCategoryItemId as uuid) is null or c.educator_category_item_id = cast(:educatorCategoryItemId as uuid))
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0)
            order by c.active desc, np.name asc, c.id asc
            """,
            countQuery = """
            select count(*)
            from class_collaborator c
            join collaborator col on col.id = c.collaborator_id
            join natural_person np on np.id = col.natural_person_id
            where c.class_group_id = :classGroupId
              and c.deleted_at is null
              and (cast(:active as boolean) is null or c.active = cast(:active as boolean))
              and (cast(:educatorCategoryItemId as uuid) is null or c.educator_category_item_id = cast(:educatorCategoryItemId as uuid))
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0)
            """,
            nativeQuery = true)
    Page<ClassCollaboratorRow> findRowsByClassGroupId(@Param("classGroupId") UUID classGroupId,
                                                      @Param("active") Boolean active,
                                                      @Param("educatorCategoryItemId") UUID educatorCategoryItemId,
                                                      @Param("searchTerm") String searchTerm,
                                                      Pageable pageable);
}
