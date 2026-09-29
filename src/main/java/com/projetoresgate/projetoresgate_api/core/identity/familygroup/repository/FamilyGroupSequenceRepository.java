package com.projetoresgate.projetoresgate_api.core.identity.familygroup.repository;

import com.projetoresgate.projetoresgate_api.core.identity.familygroup.domain.FamilyGroupSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyGroupSequenceRepository extends JpaRepository<FamilyGroupSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from FamilyGroupSequence s where s.id = :id")
    Optional<FamilyGroupSequence> findByIdForUpdate(@Param("id") Long id);
}
