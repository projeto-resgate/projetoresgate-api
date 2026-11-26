package com.projetoresgate.projetoresgate_api.core.academic.collaborator.repository;

import com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain.Collaborator;
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
public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID>,
        JpaSpecificationExecutor<Collaborator> {

    boolean existsByNaturalPersonId(UUID naturalPersonId);

    boolean existsByNaturalPersonIdAndIdNot(UUID naturalPersonId, UUID id);

    Optional<Collaborator> findByNaturalPersonId(UUID naturalPersonId);

    default Collaborator findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador não encontrado com ID: " + id));
    }

    @Query(value = """
            select c.id as "id",
                   c.natural_person_id as "naturalPersonId",
                   np.name as "naturalPersonName",
                   np.cpf as "naturalPersonCpf",
                   c.legal_person_id as "legalPersonId",
                   coalesce(nullif(trim(lp.display_name), ''), nullif(trim(lp.trade_name), ''), lp.corporate_name) as "legalPersonName",
                   c.is_voluntary as "isVoluntary"
            from collaborator c
            join natural_person np on np.id = c.natural_person_id
            left join legal_person lp on lp.id = c.legal_person_id
            where c.deleted_at is null
              and (cast(:onlyActivePeople as boolean) = false or np.deleted_at is null)
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(coalesce(np.cpf, ''), cast(:searchTerm as text)) > 0
                   or strpos(lower(coalesce(np.rg, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(lp.corporate_name, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(lp.trade_name, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(lp.display_name, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(coalesce(lp.cnpj, ''), cast(:searchTerm as text)) > 0)
            order by np.name asc, c.id asc
            """,
            countQuery = """
            select count(*)
            from collaborator c
            join natural_person np on np.id = c.natural_person_id
            left join legal_person lp on lp.id = c.legal_person_id
            where c.deleted_at is null
              and (cast(:onlyActivePeople as boolean) = false or np.deleted_at is null)
              and (cast(:searchTerm as text) is null
                   or strpos(lower(np.name), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(np.nickname, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(coalesce(np.cpf, ''), cast(:searchTerm as text)) > 0
                   or strpos(lower(coalesce(np.rg, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(lp.corporate_name, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(lp.trade_name, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(lower(coalesce(lp.display_name, '')), lower(cast(:searchTerm as text))) > 0
                   or strpos(coalesce(lp.cnpj, ''), cast(:searchTerm as text)) > 0)
            """,
            nativeQuery = true)
    Page<CollaboratorRow> searchRows(@Param("searchTerm") String searchTerm,
                                     @Param("onlyActivePeople") boolean onlyActivePeople,
                                     Pageable pageable);

    @Query(value = """
            select count(*) from class_collaborator cc
            join class_group cg on cg.id = cc.class_group_id and cg.deleted_at is null
            where cc.collaborator_id = :id and cc.deleted_at is null
            """, nativeQuery = true)
    long countActiveClassLinks(@Param("id") UUID id);
}
