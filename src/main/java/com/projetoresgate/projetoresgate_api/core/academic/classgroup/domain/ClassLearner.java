package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "class_learner")
@SQLDelete(sql = "UPDATE class_learner SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassLearner extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_group_id", nullable = false)
    private ClassGroup classGroup;

    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;

    @Column(nullable = false)
    private boolean active = true;

    protected ClassLearner() {
    }

    private ClassLearner(UUID id, ClassGroup classGroup, UUID learnerId) {
        this.id = id;
        this.classGroup = classGroup;
        this.learnerId = learnerId;
        this.active = true;
        validate();
    }

    public static ClassLearner create(ClassGroup classGroup, UUID learnerId) {
        return new ClassLearner(UUID.randomUUID(), classGroup, learnerId);
    }

    public void validate() {
        if (!nonNull(this.classGroup)) {
            throw new InternalException("A turma do educando é obrigatória.");
        }
        if (!nonNull(this.learnerId)) {
            throw new InternalException("O educando é obrigatório.");
        }
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public UUID getLearnerId() {
        return learnerId;
    }

    public boolean isActive() {
        return active;
    }
}
