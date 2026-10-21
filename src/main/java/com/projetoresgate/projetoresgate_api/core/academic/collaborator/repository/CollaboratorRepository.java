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
            select c from Collaborator c
            join NaturalPerson np on np.id = c.naturalPersonId
            left join LegalPerson lp on lp.id = c.legalPersonId
            where (:searchTerm is null
                   or lower(np.name) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or lower(np.nickname) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or np.cpf like concat('%', cast(:searchTerm as String), '%')
                   or upper(np.rg) like upper(concat('%', cast(:searchTerm as String), '%'))
                   or lower(lp.corporateName) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or lower(lp.tradeName) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or lower(lp.displayName) like lower(concat('%', cast(:searchTerm as String), '%'))
                   or lp.cnpj like concat('%', cast(:searchTerm as String), '%'))
            order by np.name asc
            """,
            countQuery = """
                    select count(c) from Collaborator c
                    join NaturalPerson np on np.id = c.naturalPersonId
                    left join LegalPerson lp on lp.id = c.legalPersonId
                    where (:searchTerm is null
                           or lower(np.name) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or lower(np.nickname) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or np.cpf like concat('%', cast(:searchTerm as String), '%')
                           or upper(np.rg) like upper(concat('%', cast(:searchTerm as String), '%'))
                           or lower(lp.corporateName) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or lower(lp.tradeName) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or lower(lp.displayName) like lower(concat('%', cast(:searchTerm as String), '%'))
                           or lp.cnpj like concat('%', cast(:searchTerm as String), '%'))
                    """)
    Page<Collaborator> search(@Param("searchTerm") String searchTerm, Pageable pageable);
}
