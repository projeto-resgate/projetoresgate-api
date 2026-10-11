package com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroup;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPerson;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

    interface NaturalPersonNameProjection {
        UUID getFamilyGroupId();

        String getName();
    }

    @Query("""
            select fg.id as familyGroupId, np.name as name
            from FamilyGroup fg
            join fg.naturalPersonList np
            where fg.id in :ids
            order by fg.id, np.name
            """)
    List<NaturalPersonNameProjection> findNaturalPersonNamesByFamilyGroupIds(@Param("ids") Collection<UUID> ids);

    @Query(value = "select nextval('family_group_friendly_id_seq')", nativeQuery = true)
    Long nextFriendlyIdValue();

    @Query("""
            select np from NaturalPerson np
            where np.id in (
                select linked.id from FamilyGroup fg join fg.naturalPersonList linked
                where fg.id = :familyGroupId
            )
            """)
    Page<NaturalPerson> findNaturalPersonsByFamilyGroupId(@Param("familyGroupId") UUID familyGroupId,
                                                          Pageable pageable);

    @Query("""
            select np from NaturalPerson np
            where np.id in (
                select linked.id from FamilyGroup fg join fg.naturalPersonList linked
                where fg.id = :familyGroupId and linked.id <> :excludeNaturalPersonId
            )
            """)
    Page<NaturalPerson> findEligibleResponsiblesByFamilyGroupId(@Param("familyGroupId") UUID familyGroupId,
                                                                @Param("excludeNaturalPersonId") UUID excludeNaturalPersonId,
                                                                Pageable pageable);

    @Query(value = """
            select exists (
                select 1 from family_group_natural_person
                where family_group_id = :familyGroupId and natural_person_id = :naturalPersonId
            )
            """, nativeQuery = true)
    boolean existsNaturalPersonMember(@Param("familyGroupId") UUID familyGroupId,
                                      @Param("naturalPersonId") UUID naturalPersonId);

    @Modifying
    @Query(value = """
            insert into family_group_natural_person (family_group_id, natural_person_id)
            values (:familyGroupId, :naturalPersonId)
            on conflict do nothing
            """, nativeQuery = true)
    int linkNaturalPerson(@Param("familyGroupId") UUID familyGroupId,
                          @Param("naturalPersonId") UUID naturalPersonId);

    @Modifying
    @Query(value = """
            delete from family_group_natural_person
            where family_group_id = :familyGroupId and natural_person_id = :naturalPersonId
            """, nativeQuery = true)
    int unlinkNaturalPerson(@Param("familyGroupId") UUID familyGroupId,
                            @Param("naturalPersonId") UUID naturalPersonId);

    default FamilyGroup findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo familiar não encontrado com ID: " + id));
    }

    interface NaturalPersonCountProjection {
        UUID getFamilyGroupId();

        Long getRegisteredPeopleCount();
    }
}
