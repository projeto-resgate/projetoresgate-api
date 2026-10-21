package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "class_collaborator")
@SQLDelete(sql = "UPDATE class_collaborator SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ClassCollaborator extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_group_id", nullable = false)
    private ClassGroup classGroup;

    @Column(name = "collaborator_id", nullable = false)
    private UUID collaboratorId;

    @Column(name = "educator_category_item_id", nullable = false)
    private UUID educatorCategoryItemId;

    @Column(nullable = false)
    private boolean active = true;

    protected ClassCollaborator() {
    }

    private ClassCollaborator(UUID id, ClassGroup classGroup, UUID collaboratorId, UUID educatorCategoryItemId) {
        this.id = id;
        this.classGroup = classGroup;
        this.collaboratorId = collaboratorId;
        this.educatorCategoryItemId = educatorCategoryItemId;
        this.active = true;
        validate();
    }

    public static ClassCollaborator create(ClassGroup classGroup, UUID collaboratorId, UUID educatorCategoryItemId) {
        return new ClassCollaborator(UUID.randomUUID(), classGroup, collaboratorId, educatorCategoryItemId);
    }

    public void validate() {
        if (!nonNull(this.classGroup)) {
            throw new InternalException("A turma do educador é obrigatória.");
        }
        if (!nonNull(this.collaboratorId)) {
            throw new InternalException("O colaborador do educador é obrigatório.");
        }
        if (!nonNull(this.educatorCategoryItemId)) {
            throw new InternalException("O tipo do educador é obrigatório.");
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

        public Updater educatorCategoryItemId(UUID educatorCategoryItemId) {
            if (nonNull(educatorCategoryItemId)) {
                ClassCollaborator.this.educatorCategoryItemId = educatorCategoryItemId;
            }
            return this;
        }

        public Updater active(Boolean active) {
            if (nonNull(active)) {
                ClassCollaborator.this.active = active;
            }
            return this;
        }

        public ClassCollaborator apply() {
            ClassCollaborator.this.validate();
            return ClassCollaborator.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public UUID getCollaboratorId() {
        return collaboratorId;
    }

    public UUID getEducatorCategoryItemId() {
        return educatorCategoryItemId;
    }

    public boolean isActive() {
        return active;
    }
}
