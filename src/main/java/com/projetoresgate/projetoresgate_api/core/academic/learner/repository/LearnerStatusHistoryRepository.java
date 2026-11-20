package com.projetoresgate.projetoresgate_api.core.academic.learner.repository;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.LearnerStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearnerStatusHistoryRepository extends JpaRepository<LearnerStatusHistory, UUID> {

    List<LearnerStatusHistory> findAllByLearnerIdOrderByEventDateDescDateCreatedDesc(UUID learnerId);
}
