package com.projetoresgate.projetoresgate_api.core.academic.collaborator.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "collaborator")
@SQLDelete(sql = "UPDATE collaborator SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Collaborator extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "is_voluntary", nullable = false)
    private boolean isVoluntary;

    @Column(name = "natural_person_id", nullable = false)
    private UUID naturalPersonId;

    @Column(name = "legal_person_id")
    private UUID legalPersonId;

    protected Collaborator() {
    }

    private Collaborator(UUID id, boolean isVoluntary, UUID naturalPersonId, UUID legalPersonId) {
        this.id = id;
        this.isVoluntary = isVoluntary;
        this.naturalPersonId = naturalPersonId;
        this.legalPersonId = legalPersonId;
        validate();
    }

    public static Collaborator create(boolean isVoluntary, UUID naturalPersonId, UUID legalPersonId) {
        return new Collaborator(UUID.randomUUID(), isVoluntary, naturalPersonId, legalPersonId);
    }

    public void validate() {
        if (!nonNull(this.naturalPersonId)) {
            throw new InternalException("A pessoa física do colaborador é obrigatória.");
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater isVoluntary(Boolean isVoluntary) {
            if (nonNull(isVoluntary)) {
                Collaborator.this.isVoluntary = isVoluntary;
            }
            return this;
        }

        public Updater naturalPersonId(UUID naturalPersonId) {
            if (nonNull(naturalPersonId)) {
                Collaborator.this.naturalPersonId = naturalPersonId;
            }
            return this;
        }

        public Updater legalPersonId(UUID legalPersonId) {
            Collaborator.this.legalPersonId = legalPersonId;
            return this;
        }

        public Collaborator apply() {
            Collaborator.this.validate();
            return Collaborator.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public boolean isVoluntary() {
        return isVoluntary;
    }

    public UUID getNaturalPersonId() {
        return naturalPersonId;
    }

    public UUID getLegalPersonId() {
        return legalPersonId;
    }
}
