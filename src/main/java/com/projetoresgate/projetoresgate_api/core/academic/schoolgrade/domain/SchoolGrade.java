package com.projetoresgate.projetoresgate_api.core.academic.schoolgrade.domain;

import com.projetoresgate.projetoresgate_api.infrastructure.exception.InternalException;
import com.projetoresgate.projetoresgate_api.shared.entity.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Entity
@Table(name = "school_grade")
@SQLDelete(sql = "UPDATE school_grade SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SchoolGrade extends AuditableEntity {

    private static final int NAME_MAX_LENGTH = 255;

    @Id
    private UUID id;

    private String name;

    private Integer gradeOrder;

    protected SchoolGrade() {
    }

    private SchoolGrade(UUID id, String name, Integer gradeOrder) {
        this.id = id;
        this.name = name;
        this.gradeOrder = gradeOrder;
        validate();
    }

    public static SchoolGrade create(String name, Integer gradeOrder) {
        return new SchoolGrade(UUID.randomUUID(), name, gradeOrder);
    }

    public void validate() {
        if (!StringUtils.hasText(this.name)) {
            throw new InternalException("O nome da série escolar não pode ser vazio.");
        }
        if (this.name.length() > NAME_MAX_LENGTH) {
            throw new InternalException("O nome da série escolar não pode exceder 255 caracteres.");
        }
        if (!nonNull(this.gradeOrder)) {
            throw new InternalException("A ordem da série escolar é obrigatória.");
        }
        if (this.gradeOrder < 1) {
            throw new InternalException("A ordem da série escolar deve ser maior que zero.");
        }
    }

    public Updater update() {
        return new Updater();
    }

    public class Updater {

        public Updater name(String name) {
            SchoolGrade.this.name = name;
            return this;
        }

        public Updater gradeOrder(Integer gradeOrder) {
            SchoolGrade.this.gradeOrder = gradeOrder;
            return this;
        }

        public SchoolGrade apply() {
            SchoolGrade.this.validate();
            return SchoolGrade.this;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getGradeOrder() {
        return gradeOrder;
    }
}
