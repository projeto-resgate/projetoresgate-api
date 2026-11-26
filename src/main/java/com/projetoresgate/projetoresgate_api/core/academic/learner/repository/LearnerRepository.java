package com.projetoresgate.projetoresgate_api.core.academic.learner.repository;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Learner;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearnerRepository extends JpaRepository<Learner, UUID>, JpaSpecificationExecutor<Learner> {

    boolean existsByNaturalPersonId(UUID naturalPersonId);

    Optional<Learner> findByNaturalPersonId(UUID naturalPersonId);

    List<Learner> findAllByNaturalPersonIdIn(Collection<UUID> naturalPersonIds);

    default Learner findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Educando não encontrado com ID: " + id));
    }

    @Query(value = "SELECT * FROM learner WHERE id IN (:ids)", nativeQuery = true)
    List<Learner> findAllByIdIncludingDeleted(@Param("ids") Collection<UUID> ids);

    default Optional<Learner> findByIdIncludingDeleted(UUID id) {
        return findAllByIdIncludingDeleted(List.of(id)).stream().findFirst();
    }

    @Query(value = """
            select l.id as "id",
                   np.name as "name",
                   np.cpf as "cpf",
                   l.status as "status"
            from learner l
            join natural_person np on np.id = l.natural_person_id and np.deleted_at is null
            where l.deleted_at is null
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(coalesce(np.cpf, ''), cast(:searchTerm as text)) > 0)
            order by l.date_created desc, l.id asc
            limit :limit
            """, nativeQuery = true)
    List<LearnerAutocompleteRow> autocomplete(@Param("searchTerm") String searchTerm,
                                              @Param("limit") int limit);
}
