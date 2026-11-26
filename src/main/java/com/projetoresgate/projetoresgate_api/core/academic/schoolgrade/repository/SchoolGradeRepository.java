package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.repository;

import com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain.SchoolGrade;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SchoolGradeRepository extends JpaRepository<SchoolGrade, UUID>,
        JpaSpecificationExecutor<SchoolGrade> {

    default SchoolGrade findByIdOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Série escolar não encontrada com ID: " + id));
    }

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    interface SchoolGradeDependencies {
        long getClassGroups();

        long getLearners();

        long getResponsibles();
    }

    @Query(value = """
            select (select count(distinct cg.id)
                    from class_school_grade csg
                    join class_group cg on cg.id = csg.class_group_id and cg.deleted_at is null
                    where csg.school_grade_id = :id) as "classGroups",
                   (select count(*) from learner l
                    where l.school_grade_id = :id and l.deleted_at is null) as "learners",
                   (select count(*) from learner_responsible lr
                    join learner l on l.id = lr.learner_id and l.deleted_at is null
                    where lr.school_grade_id = :id and lr.deleted_at is null) as "responsibles"
            """, nativeQuery = true)
    SchoolGradeDependencies countDependencies(@Param("id") UUID id);
}
