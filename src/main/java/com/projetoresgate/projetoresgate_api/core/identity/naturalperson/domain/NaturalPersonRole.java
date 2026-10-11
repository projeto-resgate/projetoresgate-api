package com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain;

import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "natural_person_role")
@SQLDelete(sql = "UPDATE natural_person_role SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class NaturalPersonRole extends AuditableEntity {

    @Id
    private UUID id;

    private UUID naturalPersonId;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private NaturalPersonRoleType role;

    protected NaturalPersonRole() {
    }

    private NaturalPersonRole(UUID id, UUID naturalPersonId, NaturalPersonRoleType role) {
        this.id = id;
        this.naturalPersonId = naturalPersonId;
        this.role = role;
        validate();
    }

    public static NaturalPersonRole create(UUID naturalPersonId, NaturalPersonRoleType role) {
        return new NaturalPersonRole(UUID.randomUUID(), naturalPersonId, role);
    }

    public void validate() {
        if (!nonNull(this.naturalPersonId)) {
            throw new InternalException("A pessoa física do papel é obrigatória.");
        }

        if (!nonNull(this.role)) {
            throw new InternalException("O papel da pessoa física é obrigatório.");
        }
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getNaturalPersonId() {
        return this.naturalPersonId;
    }

    public NaturalPersonRoleType getRole() {
        return this.role;
    }
}