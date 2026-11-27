package com.projetoresgate.projetoresgate_api.core.academic.learner.domain;

import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.Kinship;
import com.projetoresgate.projetoresgate_api.core.academic.learner.domain.enums.ResponsibleType;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.NaturalPersonRoleHolder;
import com.projetoresgate.projetoresgate_api.core.identity.naturalperson.domain.enums.NaturalPersonRoleType;
import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "learner_responsible")
@SQLDelete(sql = "UPDATE learner_responsible SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Responsible extends AuditableEntity implements NaturalPersonRoleHolder {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private Learner learner;

    private UUID naturalPersonId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "learner_responsible_type", joinColumns = @JoinColumn(name = "learner_responsible_id"))
    @Column(name = "responsible_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<ResponsibleType> types = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Kinship kinship;

    @Column(name = "school_grade_id")
    private UUID schoolGradeId;

    protected Responsible() {
    }

    private Responsible(UUID id, Learner learner, UUID naturalPersonId, Collection<ResponsibleType> types,
                        Kinship kinship, UUID schoolGradeId) {
        this.id = id;
        this.learner = learner;
        this.naturalPersonId = naturalPersonId;
        replaceTypes(types);
        this.kinship = kinship;
        this.schoolGradeId = schoolGradeId;
        validate();
    }

    public static Responsible create(Learner learner, UUID naturalPersonId, Collection<ResponsibleType> types,
                                     Kinship kinship, UUID schoolGradeId) {
        return new Responsible(UUID.randomUUID(), learner, naturalPersonId, types, kinship, schoolGradeId);
    }

    // Mantém a mesma instância da coleção, que o Hibernate acompanha; tipos repetidos contam uma vez só
    private void replaceTypes(Collection<ResponsibleType> newTypes) {
        List<ResponsibleType> incoming = nonNull(newTypes)
                ? newTypes.stream().filter(Objects::nonNull).toList()
                : List.of();
        this.types.clear();
        this.types.addAll(incoming);
    }

    public void validate() {
        if (!nonNull(this.learner)) {
            throw new InternalException("O educando do responsável é obrigatório.");
        }
        if (!nonNull(this.naturalPersonId)) {
            throw new InternalException("A pessoa física do responsável é obrigatória.");
        }
        if (this.types.isEmpty()) {
            throw new InternalException("Informe ao menos um tipo de responsabilidade.");
        }
        if (!nonNull(this.kinship)) {
            throw new InternalException("O parentesco do responsável é obrigatório.");
        }
    }

    public void updateFrom(Responsible changes) {
        this.naturalPersonId = changes.naturalPersonId;
        replaceTypes(changes.types);
        this.kinship = changes.kinship;
        this.schoolGradeId = changes.schoolGradeId;
        validate();
    }

    public UUID getId() {
        return id;
    }

    public Learner getLearner() {
        return learner;
    }

    public UUID getNaturalPersonId() {
        return naturalPersonId;
    }

    @Override
    public UUID naturalPersonId() {
        return naturalPersonId;
    }

    @Override
    public NaturalPersonRoleType role() {
        return NaturalPersonRoleType.RESPONSIBLE;
    }

    public List<ResponsibleType> getTypes() {
        return types.stream().sorted().toList();
    }

    public Kinship getKinship() {
        return kinship;
    }

    public UUID getSchoolGradeId() {
        return schoolGradeId;
    }
}