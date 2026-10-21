package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain;

import com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain.enums.WeekDay;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "class_schedule")
@SQLDelete(sql = "UPDATE class_schedule SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassSchedule extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_group_id", nullable = false)
    private ClassGroup classGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_day", nullable = false, length = 20)
    private WeekDay weekDay;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;

    protected ClassSchedule() {
    }

    private ClassSchedule(UUID id, ClassGroup classGroup, WeekDay weekDay, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.classGroup = classGroup;
        this.weekDay = weekDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = true;
        validate();
    }

    public static ClassSchedule create(ClassGroup classGroup, WeekDay weekDay, LocalTime startTime, LocalTime endTime) {
        return new ClassSchedule(UUID.randomUUID(), classGroup, weekDay, startTime, endTime);
    }

    public void validate() {
        if (!nonNull(this.classGroup)) {
            throw new InternalException("A turma do horário é obrigatória.");
        }
        if (!nonNull(this.weekDay)) {
            throw new InternalException("O dia da semana do horário é obrigatório.");
        }
        if (!nonNull(this.startTime)) {
            throw new InternalException("O horário de início é obrigatório.");
        }
        if (!nonNull(this.endTime)) {
            throw new InternalException("O horário de término é obrigatório.");
        }
        if (!this.endTime.isAfter(this.startTime)) {
            throw new InternalException("O horário de término deve ser posterior ao horário de início.");
        }
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater weekDay(WeekDay weekDay) {
            if (nonNull(weekDay)) {
                ClassSchedule.this.weekDay = weekDay;
            }
            return this;
        }

        public Updater startTime(LocalTime startTime) {
            if (nonNull(startTime)) {
                ClassSchedule.this.startTime = startTime;
            }
            return this;
        }

        public Updater endTime(LocalTime endTime) {
            if (nonNull(endTime)) {
                ClassSchedule.this.endTime = endTime;
            }
            return this;
        }

        public ClassSchedule apply() {
            ClassSchedule.this.validate();
            return ClassSchedule.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public WeekDay getWeekDay() {
        return weekDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isActive() {
        return active;
    }
}
