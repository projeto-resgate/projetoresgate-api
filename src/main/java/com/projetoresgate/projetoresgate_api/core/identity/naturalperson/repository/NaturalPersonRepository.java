package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository;

import java.util.List;


import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NaturalPersonRepository extends JpaRepository<NaturalPerson, UUID>, JpaSpecificationExecutor<NaturalPerson> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, UUID id);

    boolean existsByRg(String rg);

    boolean existsByRgAndIdNot(String rg, UUID id);

    List<NaturalPerson> findAllByEmailIgnoreCase(String email);

    default NaturalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa física não encontrada com ID: " + id));
    }

    interface NaturalPersonDependencies {
        long getLearners();

        long getResponsibilities();

        long getCollaborators();

        long getFamilyGroups();
    }

    @Query(value = """
            select (select count(*) from learner
                    where natural_person_id = :id and deleted_at is null) as "learners",
                   (select count(*) from learner_responsible lr
                    join learner l on l.id = lr.learner_id and l.deleted_at is null
                    where lr.natural_person_id = :id and lr.deleted_at is null) as "responsibilities",
                   (select count(*) from collaborator
                    where natural_person_id = :id and deleted_at is null) as "collaborators",
                   (select count(*) from family_group_natural_person fgnp
                    join family_group fg on fg.id = fgnp.family_group_id and fg.deleted_at is null
                    where fgnp.natural_person_id = :id) as "familyGroups"
            """, nativeQuery = true)
    NaturalPersonDependencies countDependencies(@Param("id") UUID id);
}
