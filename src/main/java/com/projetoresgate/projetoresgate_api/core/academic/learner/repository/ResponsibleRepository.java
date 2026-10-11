package com.projetoresgate.projetoresgate_api.core.academic.learner.repository;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.Responsible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResponsibleRepository extends JpaRepository<Responsible, UUID> {

    @Query("select r.naturalPersonId from Responsible r where r.learner.id = :learnerId and r.deletedAt is null")
    List<UUID> findNaturalPersonIdsByLearnerId(@Param("learnerId") UUID learnerId);

    @Query("select count(r) from Responsible r where r.naturalPersonId = :naturalPersonId and r.deletedAt is null")
    long countActiveByNaturalPersonId(@Param("naturalPersonId") UUID naturalPersonId);

    @Modifying
    @Query(value = """
            update learner_responsible
            set deleted_at = now()
            where learner_id = :learnerId and deleted_at is null
            """, nativeQuery = true)
    void softDeleteByLearnerId(@Param("learnerId") UUID learnerId);
}