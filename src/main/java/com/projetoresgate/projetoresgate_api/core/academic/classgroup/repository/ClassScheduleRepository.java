package com.projetoresgate.projetoresgate_api.core.academic.classgroup.repository;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.ClassSchedule;
import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID>,
        JpaSpecificationExecutor<ClassSchedule> {

    Optional<ClassSchedule> findByIdAndClassGroupId(UUID id, UUID classGroupId);

    default ClassSchedule findByIdAndClassGroupIdOrThrow(UUID id, UUID classGroupId) {
        return findByIdAndClassGroupId(id, classGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Horário não encontrado com ID: " + id + " na turma: " + classGroupId));
    }

    @Query("select distinct s.weekDay from ClassSchedule s where s.classGroup.id = :classGroupId and s.active = true")
    List<WeekDay> findActiveWeekDaysByClassGroupId(@Param("classGroupId") UUID classGroupId);

    @Query("select distinct s.classGroup.id, s.weekDay from ClassSchedule s "
            + "where s.classGroup.id in :classGroupIds and s.active = true")
    List<Object[]> findActiveWeekDaysByClassGroupIds(@Param("classGroupIds") List<UUID> classGroupIds);

    @Modifying
    @Query(value = """
            update class_schedule
            set deleted_at = now()
            where class_group_id = :classGroupId and deleted_at is null
            """, nativeQuery = true)
    void softDeleteByClassGroupId(@Param("classGroupId") UUID classGroupId);
}
