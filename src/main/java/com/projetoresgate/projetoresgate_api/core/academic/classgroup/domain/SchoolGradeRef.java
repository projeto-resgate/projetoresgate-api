package com.projetoresgate.projetoresgate_api.core.academic.classgroup.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Immutable
@SQLRestriction("deleted_at IS NULL")
@Table(name = "school_grade")
public class SchoolGradeRef {

    @Id
    private UUID id;

    private String name;

    private String abbreviation;

    private Integer gradeOrder;

    protected SchoolGradeRef() {
    }

    public static SchoolGradeRef of(UUID id, String name, String abbreviation, Integer gradeOrder) {
        SchoolGradeRef ref = new SchoolGradeRef();
        ref.id = id;
        ref.name = name;
        ref.abbreviation = abbreviation;
        ref.gradeOrder = gradeOrder;
        return ref;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public Integer getGradeOrder() {
        return gradeOrder;
    }
}
