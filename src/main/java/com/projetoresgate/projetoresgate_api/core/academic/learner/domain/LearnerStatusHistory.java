package com.projetoresgate.projetoresgate_api.core.academic.learner.domain;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.LearnerStatusEventType;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "learner_status_history")
@SQLDelete(sql = "UPDATE learner_status_history SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class LearnerStatusHistory extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private Learner learner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearnerStatusEventType type;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    protected LearnerStatusHistory() {
    }

    private LearnerStatusHistory(UUID id, Learner learner, LearnerStatusEventType type, String reason,
                                 LocalDate eventDate) {
        this.id = id;
        this.learner = learner;
        this.type = type;
        this.reason = reason;
        this.eventDate = eventDate;
        validate();
    }

    public static LearnerStatusHistory create(Learner learner, LearnerStatusEventType type, String reason,
                                              LocalDate eventDate) {
        return new LearnerStatusHistory(UUID.randomUUID(), learner, type, reason, eventDate);
    }

    public void validate() {
        if (!nonNull(this.learner)) {
            throw new InternalException("O educando do histórico de status é obrigatório.");
        }
        if (!nonNull(this.type)) {
            throw new InternalException("O tipo do histórico de status é obrigatório.");
        }
        if (!nonNull(this.eventDate)) {
            throw new InternalException("A data do histórico de status é obrigatória.");
        }
        if (this.type == LearnerStatusEventType.DROPOUT && !StringUtils.hasText(this.reason)) {
            throw new InternalException("O motivo da desistência é obrigatório.");
        }
    }

    public UUID getId() {
        return id;
    }

    public Learner getLearner() {
        return learner;
    }

    public LearnerStatusEventType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }
}
