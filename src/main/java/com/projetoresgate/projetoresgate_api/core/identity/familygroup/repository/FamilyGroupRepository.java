package com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, UUID>, JpaSpecificationExecutor<FamilyGroup> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Query("""
            select fg.id as familyGroupId, count(np.id) as registeredPeopleCount
            from FamilyGroup fg
            left join fg.naturalPersonList np
            where fg.id in :ids
            group by fg.id
            """)
    List<NaturalPersonCountProjection> countRegisteredPeopleByIds(@Param("ids") Collection<UUID> ids);

    default FamilyGroup findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));
    }

    interface NaturalPersonCountProjection {
        UUID getFamilyGroupId();

        Long getRegisteredPeopleCount();
    }
}
