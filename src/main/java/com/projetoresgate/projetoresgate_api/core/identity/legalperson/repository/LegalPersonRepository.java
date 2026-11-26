package com.projetoresgate.projetoresgate_api.core.identity.legalperson.repository;




import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Query;

import com.projetoresgate.projetoresgate_api.core.identity.legalperson.domain.LegalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LegalPersonRepository extends JpaRepository<LegalPerson, UUID>, JpaSpecificationExecutor<LegalPerson> {

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, UUID id);

    default LegalPerson findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa jurídica não encontrada com ID: " + id));
    }

    interface LegalPersonDependencies {
        long getClassGroups();

        long getPrograms();

        long getCollaborators();

        long getLearners();

        long getWorkplaces();
    }

    @Query(value = """
            select (select count(*) from class_group
                    where institution_id = :id and deleted_at is null) as "classGroups",
                   (select count(*) from program
                    where institution_id = :id and deleted_at is null) as "programs",
                   (select count(*) from collaborator
                    where legal_person_id = :id and deleted_at is null) as "collaborators",
                   (select count(*) from learner
                    where institution_id = :id and deleted_at is null) as "learners",
                   (select count(*) from natural_person
                    where workplace_id = :id and deleted_at is null) as "workplaces"
            """, nativeQuery = true)
    LegalPersonDependencies countDependencies(@Param("id") UUID id);
}
