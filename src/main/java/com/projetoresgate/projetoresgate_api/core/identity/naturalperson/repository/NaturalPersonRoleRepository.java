package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.repository;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRole;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NaturalPersonRoleRepository extends JpaRepository<NaturalPersonRole, UUID> {

    Optional<NaturalPersonRole> findByNaturalPersonIdAndRole(UUID naturalPersonId, NaturalPersonRoleType role);

    @Query("""
            select r.naturalPersonId as naturalPersonId, r.role as role
            from NaturalPersonRole r
            where r.naturalPersonId in :naturalPersonIds
            """)
    List<NaturalPersonRoleProjection> findRolesByNaturalPersonIdsIn(@Param("naturalPersonIds") Collection<UUID> naturalPersonIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NaturalPersonRole r
            set r.deletedAt = :deletedAt
            where r.naturalPersonId = :naturalPersonId and r.role = :role and r.deletedAt is null
            """)
    int revokeByNaturalPersonIdAndRole(@Param("naturalPersonId") UUID naturalPersonId,
                                       @Param("role") NaturalPersonRoleType role,
                                       @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NaturalPersonRole r
            set r.deletedAt = :deletedAt
            where r.naturalPersonId = :naturalPersonId and r.deletedAt is null
            """)
    int revokeAllByNaturalPersonId(@Param("naturalPersonId") UUID naturalPersonId,
                                   @Param("deletedAt") LocalDateTime deletedAt);

    interface NaturalPersonRoleProjection {
        UUID getNaturalPersonId();

        NaturalPersonRoleType getRole();
    }
}